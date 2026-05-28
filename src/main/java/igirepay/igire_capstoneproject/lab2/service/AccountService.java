package igirepay.igire_capstoneproject.lab2.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab1.model.SavingsAccount;
import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab1.model.WalletAccount;
import igirepay.igire_capstoneproject.lab1.util.FeeCalculator;
import igirepay.igire_capstoneproject.lab1.util.ValidationUtils;
import igirepay.igire_capstoneproject.lab2.dao.AccountDAO;
import igirepay.igire_capstoneproject.lab2.dao.ProcessedRequestDAO;
import igirepay.igire_capstoneproject.lab2.dao.TransactionDAO;
import igirepay.igire_capstoneproject.lab2.dao.impl.AccountDAOImpl;
import igirepay.igire_capstoneproject.lab2.dao.impl.ProcessedRequestDAOImpl;
import igirepay.igire_capstoneproject.lab2.dao.impl.TransactionDAOImpl;

public class AccountService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final ProcessedRequestDAO processedRequestDAO;

    public AccountService() {
        this(new AccountDAOImpl(), new TransactionDAOImpl(), new ProcessedRequestDAOImpl());
    }

    public AccountService(AccountDAO accountDAO,
                           TransactionDAO transactionDAO,
                           ProcessedRequestDAO processedRequestDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.processedRequestDAO = processedRequestDAO;
    }


    public Account findAccountByNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return null;
        }
        return accountDAO.findByAccountNumber(accountNumber.trim());
    }

    public String createWalletAccount(Customer customer, String pin) {
        if (!ValidationUtils.isValidPin(pin)) {
            return "Invalid PIN. Must be 5 digits";
        }

        String accountNumber = UUID.randomUUID().toString();
        Account wallet = new WalletAccount(accountNumber, pin, customer.getFullName());
        accountDAO.create(wallet, customer.getCustomerId(), "WALLET");
        return accountNumber;
    }

    public String createSavingsAccount(Customer customer, String pin) {
        if (!ValidationUtils.isValidPin(pin)) {
            return "Invalid PIN. Must be 5 digits";
        }

        String accountNumber = UUID.randomUUID().toString();
        Account savings = new SavingsAccount(accountNumber, pin, customer.getFullName());
        accountDAO.create(savings, customer.getCustomerId(), "SAVINGS");
        return accountNumber;
    }

    public double checkBalance(Account account, String pin) {
        if (!validatePin(account, pin)) {
            return -1.0;
        }
        if (account.isLocked()) {
            return -2.0;
        }
        // persist pinAttempts/lock if it changed during validatePin
        accountDAO.update(account);
        return account.getBalance();
    }

    public String deposit(Account account, double amount, UUID referenceId, TransactionService txService) {
        if (!ValidationUtils.isValidAmount(amount)) {
            return "Invalid amount";
        }
        if (processedRequestDAO.existsByReferenceId(referenceId)) {
            return "Duplicate transaction reference";
        }

        account.deposit(amount);
        Transaction tx = new Transaction(referenceId, amount, 0.0, "DEPOSIT",
                account.getAccountNumber(), null, "SUCCESS");
        accountDAO.update(account);
        txService.recordAndMarkProcessed(tx);
        return "SUCCESS";
    }

    public String withdraw(Account account, String pin, double amount, UUID referenceId, TransactionService txService) {
        if (!ValidationUtils.isValidAmount(amount)) {
            return "Invalid amount";
        }
        if (processedRequestDAO.existsByReferenceId(referenceId)) {
            return "Duplicate transaction reference";
        }

        if (!validatePin(account, pin)) {
            accountDAO.update(account);
            return account.isLocked() ? "Account locked after 3 wrong PIN attempts" : "Invalid PIN";
        }

        if (account.isLocked()) {
            accountDAO.update(account);
            return "Account is locked";
        }

        if (!checkDailyWithdrawalLimit(account, amount)) {
            accountDAO.update(account);
            return "Daily withdrawal limit of 200,000 RWF exceeded";
        }

        if (!account.withdraw(amount)) {
            accountDAO.update(account);
            return "Insufficient funds";
        }

        // update daily tracking
        account.setDailyWithdrawnAmount(account.getDailyWithdrawnAmount() + amount);
        Transaction tx = new Transaction(referenceId, amount, 0.0, "WITHDRAW",
                account.getAccountNumber(), null, "SUCCESS");

        accountDAO.update(account);
        txService.recordAndMarkProcessed(tx);
        return "SUCCESS";
    }

    public String transfer(Account source, String pin, Account destination, double amount, UUID referenceId, TransactionService txService) {
        if (!ValidationUtils.isValidAmount(amount)) {
            return "Invalid amount";
        }
        if (processedRequestDAO.existsByReferenceId(referenceId)) {
            return "Duplicate transaction reference";
        }

        if (!validatePin(source, pin)) {
            accountDAO.update(source);
            return source.isLocked() ? "Source account locked after 3 wrong PIN attempts" : "Invalid PIN";
        }

        if (source.isLocked()) {
            accountDAO.update(source);
            return "Source account locked";
        }

        double fee = FeeCalculator.calculateTransferFee(amount);
        double totalDeduction = amount + fee;

        if (source.getBalance() < totalDeduction) {
            accountDAO.update(source);
            return "Insufficient funds for transfer + fee";
        }

        // Withdrawal from source (wallet/savings models treat withdraw differently)
        // For transfer we apply deduction directly to balance to match Lab1 behavior.
        source.setBalance(source.getBalance() - totalDeduction);
        destination.deposit(amount);

        Transaction tx = new Transaction(referenceId, amount, fee, "TRANSFER",
                source.getAccountNumber(), destination.getAccountNumber(), "SUCCESS");

        accountDAO.update(source);
        accountDAO.update(destination);
        txService.recordAndMarkProcessed(tx);

        return "SUCCESS";
    }

    private boolean validatePin(Account account, String enteredPin) {
        if (account.isLocked()) {
            return false;
        }
        if (account.getPin().equals(enteredPin)) {
            account.setPinAttempts(0);
            return true;
        }

        int attempts = account.getPinAttempts() + 1;
        account.setPinAttempts(attempts);
        if (attempts >= 3) {
            account.setLocked(true);
        }
        return false;
    }

    private boolean checkDailyWithdrawalLimit(Account account, double amount) {
        LocalDate today = LocalDate.now();
        if (!today.equals(account.getLastWithdrawalDate())) {
            account.setDailyWithdrawnAmount(0.0);
            account.setLastWithdrawalDate(today);
        }
        double newTotal = account.getDailyWithdrawnAmount() + amount;
        return newTotal <= 200000.0;
    }

    public List<Transaction> viewTransactionHistoryForAccount(Account account) {
        return transactionDAO.findByAccountId(getAccountIdPlaceholder(account));
    }

    // Account model doesn't store UUID id in Lab1.
    // For now we interpret account.getAccountNumber() and let TransactionDAOImpl return transactions by account_id.
    // This method is a placeholder and will be adjusted in TransactionService wiring.
    private UUID getAccountIdPlaceholder(Account account) {
        // best-effort: look up by account number via DB and map not implemented here.
        // For now return null; TransactionService/Main will use transactionDAO.findByAccountId through its own lookup.
        return null;
    }
}

