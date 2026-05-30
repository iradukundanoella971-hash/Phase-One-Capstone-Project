package igirepay.igire_capstoneproject.lab3.controller;

import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.exception.DuplicateTransactionException;
import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab1.model.SavingsAccount;
import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab1.model.WalletAccount;
import igirepay.igire_capstoneproject.lab1.util.ValidationUtils;
import igirepay.igire_capstoneproject.lab2.dao.AccountDAO;
import igirepay.igire_capstoneproject.lab2.dao.impl.AccountDAOImpl;
import igirepay.igire_capstoneproject.lab2.service.AccountService;
import igirepay.igire_capstoneproject.lab2.service.CustomerService;
import igirepay.igire_capstoneproject.lab2.service.LoanService;
import igirepay.igire_capstoneproject.lab2.service.TransactionService;
import igirepay.igire_capstoneproject.lab2.model.Loan;
import igirepay.igire_capstoneproject.lab3.util.TransactionHistoryLoader;

public class AppController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final LoanService loanService;
    private final TransactionService transactionService;
    private final AccountDAO accountDAO;

    public AppController() {
        this.customerService = new CustomerService();
        this.accountService = new AccountService();
        this.loanService = new LoanService();
        this.transactionService = new TransactionService();
        this.accountDAO = new AccountDAOImpl();
    }

    public LoginResult loginByPhoneAndPin(String phone, String pin) {
        if (phone == null || phone.isBlank()) {
            return LoginResult.failure("Enter your phone number");
        }
        if (!ValidationUtils.isValidPin(pin)) {
            return LoginResult.failure("PIN must be 5 digits");
        }

        Customer customer = customerService.listCustomers().stream()
                .filter(c -> phone.trim().equals(c.getPhoneNumber()))
                .findFirst()
                .orElse(null);

        if (customer == null) {
            return LoginResult.failure("Invalid phone number or PIN");
        }

        Account wallet = findWalletForCustomer(customer);
        if (wallet == null) {
            return LoginResult.failure("No wallet account found. Please register first.");
        }

        double balance = accountService.checkBalance(wallet, pin);
        if (balance == -1.0) {
            return LoginResult.failure("Invalid phone number or PIN");
        }
        if (balance == -2.0 || wallet.isLocked()) {
            return LoginResult.failure("Account locked after 3 wrong PIN attempts");
        }
        return LoginResult.success(customer, wallet, pin);
    }

    public boolean isEmailTaken(String email) {
        return customerService.listCustomers().stream()
                .anyMatch(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(email));
    }

    public boolean isPhoneTaken(String phone) {
        return customerService.listCustomers().stream()
                .anyMatch(c -> c.getPhoneNumber() != null && c.getPhoneNumber().equals(phone));
    }

    public Customer findCustomerByPhone(String phone) {
        return customerService.findCustomerByPhone(phone);
    }

    public Customer registerCustomer(String fullName, String email, String phone) {
        String id = customerService.registerCustomer(fullName, email, phone);
        if (id == null || id.startsWith("Invalid")) {
            return null;
        }
        return customerService.findCustomerById(id);
    }

    public Account createWalletAccount(Customer customer, String pin) {
        String accountNumber = accountService.createWalletAccount(customer, pin);
        if (accountNumber == null || accountNumber.startsWith("Invalid")) {
            return null;
        }
        return accountService.findAccountByNumber(accountNumber);
    }

    public Account createSavingsAccount(Customer customer, String pin) {
        String accountNumber = accountService.createSavingsAccount(customer, pin);
        if (accountNumber == null || accountNumber.startsWith("Invalid")) {
            return null;
        }
        return accountService.findAccountByNumber(accountNumber);
    }

    public Account findWalletForCustomer(Customer customer) {
        return findAccountForCustomer(customer, WalletAccount.class);
    }

    public Account findSavingsForCustomer(Customer customer) {
        return findAccountForCustomer(customer, SavingsAccount.class);
    }

    public Account findPrimaryAccountByPhone(String phone) {
        Customer customer = findCustomerByPhone(phone);
        if (customer == null) {
            return null;
        }
        Account wallet = findWalletForCustomer(customer);
        if (wallet != null) {
            return wallet;
        }
        return findSavingsForCustomer(customer);
    }

    private Account findAccountForCustomer(Customer customer, Class<? extends Account> type) {
        if (customer == null) {
            return null;
        }
        String id = customer.getCustomerId().toString();
        return accountDAO.findAll().stream()
                .filter(a -> id.equals(a.getAccountHolderName()))
                .filter(type::isInstance)
                .findFirst()
                .orElse(null);
    }

    public Account findAccountByNumber(String accountNumber) {
        return accountService.findAccountByNumber(accountNumber);
    }

    public Account findWalletByPhone(String phone) {
        Customer customer = findCustomerByPhone(phone);
        return customer == null ? null : findWalletForCustomer(customer);
    }

    public Account reloadAccount(Account account) {
        return account == null ? null : accountService.findAccountByNumber(account.getAccountNumber());
    }

    public double checkBalance(Account account, String pin) {
        return accountService.checkBalance(account, pin);
    }

    public UUID generateReferenceId() {
        return transactionService.generateReferenceId();
    }

    public String deposit(Account account, double amount, UUID referenceId) throws DuplicateTransactionException {
        ensureNotDuplicate(referenceId);
        return accountService.deposit(account, amount, referenceId, transactionService);
    }

    public String withdraw(Account account, String pin, double amount, UUID referenceId)
            throws DuplicateTransactionException {
        ensureNotDuplicate(referenceId);
        return accountService.withdraw(account, pin, amount, referenceId, transactionService);
    }

    public String transfer(Account source, String pin, Account destination, double amount, UUID referenceId)
            throws DuplicateTransactionException {
        ensureNotDuplicate(referenceId);
        return accountService.transfer(source, pin, destination, amount, referenceId, transactionService);
    }

    public String transferByPhone(Account source, String pin, String receiverPhone, double amount, UUID referenceId)
            throws DuplicateTransactionException {
        if (source == null) {
            return "Source account not found";
        }

        Customer sender = refreshCustomer(source.getAccountHolderName());
        String normalizedReceiverPhone = receiverPhone == null ? "" : receiverPhone.trim();
        if (sender != null && sender.getPhoneNumber() != null
                && sender.getPhoneNumber().trim().equals(normalizedReceiverPhone)) {
            return "You cannot transfer to your own phone number.";
        }

        Account destination = findPrimaryAccountByPhone(normalizedReceiverPhone);
        if (destination == null) {
            return "Receiver phone number not found.";
        }

        return transfer(source, pin, destination, amount, referenceId);
    }

    private void ensureNotDuplicate(UUID referenceId) throws DuplicateTransactionException {
        if (transactionService.isDuplicateReference(referenceId)) {
            throw new DuplicateTransactionException();
        }
    }

    public boolean updateCustomerProfile(String customerId, String fullName, String email, String phone) {
        return customerService.updateCustomer(customerId, fullName, email, phone);
    }

    public Customer refreshCustomer(String customerId) {
        return customerService.findCustomerById(customerId);
    }

    public boolean updateAccountPin(Account account, String currentPin, String newPin) {
        if (!ValidationUtils.isValidPin(newPin)) {
            return false;
        }
        if (accountService.checkBalance(account, currentPin) < 0) {
            return false;
        }
        account.setPin(newPin);
        accountDAO.update(account);
        return true;
    }

    public List<Transaction> getTransactionHistoryForAccount(Account account) {
        return account == null ? List.of() : TransactionHistoryLoader.loadForAccount(account.getAccountNumber());
    }

    public List<Transaction> getRecentTransactions(Account account, int limit) {
        return getTransactionHistoryForAccount(account).stream().limit(limit).toList();
    }

    public List<Transaction> getTransactionHistory(Account account) {
        return getTransactionHistoryForAccount(account);
    }

    public Loan getOutstandingLoanForCustomer(Customer customer) {
        if (customer == null) {
            return null;
        }
        return loanService.findOutstandingLoan(customer.getCustomerId());
    }

    public LoanResult requestLoan(Account account, Customer customer, String pin, double amount) {
        if (account == null || customer == null) {
            return LoanResult.failure("Account not found");
        }
        if (!ValidationUtils.isValidAmount(amount)) {
            return LoanResult.failure("Enter a valid loan amount greater than 0");
        }

        double balanceCheck = accountService.checkBalance(account, pin);
        if (balanceCheck < 0) {
            return LoanResult.failure("Invalid PIN. Loan request denied.");
        }

        Loan activeLoan = loanService.findOutstandingLoan(customer.getCustomerId());
        if (activeLoan != null && !activeLoan.isPaid()) {
            return LoanResult.failure("You already have an active loan.");
        }

        Loan loan = loanService.createLoan(customer.getCustomerId(), amount);
        account.setBalance(account.getBalance() + amount);
        accountDAO.update(account);

        Transaction tx = new Transaction(UUID.randomUUID(), amount, 0.0, "LOAN_DISBURSEMENT",
                account.getAccountNumber(), "LOAN", "SUCCESS");
        transactionService.recordAndMarkProcessed(tx);

        return LoanResult.success(loan);
    }

    public LoanRepaymentResult repayLoan(Account account, Customer customer, String pin) {
        if (account == null || customer == null) {
            return LoanRepaymentResult.failure("Account not found");
        }

        double balanceCheck = accountService.checkBalance(account, pin);
        if (balanceCheck < 0) {
            return LoanRepaymentResult.failure("Invalid PIN. Loan repayment denied.");
        }

        Loan activeLoan = loanService.findOutstandingLoan(customer.getCustomerId());
        if (activeLoan == null || activeLoan.isPaid()) {
            return LoanRepaymentResult.failure("No active loan found.");
        }
        if (account.getBalance() < activeLoan.getLoanAmount()) {
            return LoanRepaymentResult.failure("Insufficient balance to repay the loan.");
        }

        account.setBalance(account.getBalance() - activeLoan.getLoanAmount());
        accountDAO.update(account);
        loanService.markPaid(activeLoan);

        Transaction tx = new Transaction(UUID.randomUUID(), activeLoan.getLoanAmount(), 0.0, "LOAN_REPAYMENT",
                account.getAccountNumber(), "LOAN", "SUCCESS");
        transactionService.recordAndMarkProcessed(tx);

        return LoanRepaymentResult.success(activeLoan);
    }

    public String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 5) {
            return "****";
        }
        return accountNumber.substring(0, 4) + "..." + accountNumber.substring(accountNumber.length() - 4);
    }

    public record LoginResult(boolean ok, Customer customer, Account account, String pin, String message) {
        public static LoginResult success(Customer customer, Account account, String pin) {
            return new LoginResult(true, customer, account, pin, null);
        }

        public static LoginResult failure(String message) {
            return new LoginResult(false, null, null, null, message);
        }
    }

    public record LoanResult(boolean ok, Loan loan, String message) {
        public static LoanResult success(Loan loan) {
            return new LoanResult(true, loan, null);
        }

        public static LoanResult failure(String message) {
            return new LoanResult(false, null, message);
        }
    }

    public record LoanRepaymentResult(boolean ok, Loan loan, String message) {
        public static LoanRepaymentResult success(Loan loan) {
            return new LoanRepaymentResult(true, loan, null);
        }

        public static LoanRepaymentResult failure(String message) {
            return new LoanRepaymentResult(false, null, message);
        }
    }
}
