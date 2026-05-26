// ===== File: AccountService.java =====
package igirepay.igire_capstoneproject.lab1.service;

import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab1.util.ValidationUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccountService {
    private List<Account> accounts;
    private List<Transaction> transactionHistory;
    private Set<String> processedRefIds;
    private Map<String, String> failedTxLogs;
    private TransactionService transactionService;

    public AccountService(List<Account> accounts, List<Transaction> transactionHistory,
                          Set<String> processedRefIds, Map<String, String> failedTxLogs,
                          TransactionService transactionService) {
        this.accounts = accounts;
        this.transactionHistory = transactionHistory;
        this.processedRefIds = processedRefIds;
        this.failedTxLogs = failedTxLogs;
        this.transactionService = transactionService;
    }

    public Account findAccountByNumber(String accountNumber) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equals(accountNumber)) {
                return acc;
            }
        }
        return null;
    }

    public boolean validatePin(Account account, String enteredPin) {
        if (account.isLocked()) {
            return false;
        }
        if (account.getPin().equals(enteredPin)) {
            account.setPinAttempts(0);
            return true;
        } else {
            int attempts = account.getPinAttempts() + 1;
            account.setPinAttempts(attempts);
            if (attempts >= 3) {
                account.setLocked(true);
                System.out.println("Account locked due to 3 wrong PIN attempts.");
            }
            return false;
        }
    }

    public boolean checkDailyWithdrawalLimit(Account account, double amount) {
        LocalDate today = LocalDate.now();
        if (!today.equals(account.getLastWithdrawalDate())) {
            account.setDailyWithdrawnAmount(0.0);
            account.setLastWithdrawalDate(today);
        }
        double newTotal = account.getDailyWithdrawnAmount() + amount;
        return newTotal <= 200000.0;
    }

    public void updateDailyWithdrawal(Account account, double amount) {
        account.setDailyWithdrawnAmount(account.getDailyWithdrawnAmount() + amount);
    }

    public String deposit(Account account, double amount, String referenceId) {
        if (!ValidationUtils.isValidAmount(amount)) {
            return "Invalid amount";
        }
        if (transactionService.isDuplicateReference(referenceId)) {
            return "Duplicate transaction reference";
        }
        account.deposit(amount);
        Transaction tx = new Transaction(referenceId, amount, 0.0, "DEPOSIT",
                account.getAccountNumber(), null, "SUCCESS");
        transactionService.recordTransaction(tx, processedRefIds, transactionHistory);
        account.processTransaction(tx);
        return "SUCCESS";
    }

    public String withdraw(Account account, String pin, double amount, String referenceId) {
        if (!ValidationUtils.isValidAmount(amount)) {
            return "Invalid amount";
        }
        if (transactionService.isDuplicateReference(referenceId)) {
            return "Duplicate transaction reference";
        }
        if (!validatePin(account, pin)) {
            return "Invalid PIN";
        }
        if (account.isLocked()) {
            return "Account is locked";
        }
        if (!checkDailyWithdrawalLimit(account, amount)) {
            return "Daily withdrawal limit of 200,000 RWF exceeded";
        }
        if (!account.withdraw(amount)) {
            return "Insufficient funds";
        }
        updateDailyWithdrawal(account, amount);
        Transaction tx = new Transaction(referenceId, amount, 0.0, "WITHDRAW",
                account.getAccountNumber(), null, "SUCCESS");
        transactionService.recordTransaction(tx, processedRefIds, transactionHistory);
        account.processTransaction(tx);
        return "SUCCESS";
    }

    public String transfer(Account source, String pin, Account destination, double amount, String referenceId) {
        if (!ValidationUtils.isValidAmount(amount)) {
            return "Invalid amount";
        }
        if (transactionService.isDuplicateReference(referenceId)) {
            return "Duplicate transaction reference";
        }
        if (!validatePin(source, pin)) {
            return "Invalid PIN";
        }
        if (source.isLocked()) {
            return "Source account locked";
        }
        double fee = igirepay.igire_capstoneproject.lab1.util.FeeCalculator.calculateTransferFee(amount);
        double totalDeduction = amount + fee;
        if (source.getBalance() < totalDeduction) {
            return "Insufficient funds for transfer + fee";
        }
        source.setBalance(source.getBalance() - totalDeduction);
        destination.deposit(amount);
        Transaction tx = new Transaction(referenceId, amount, fee, "TRANSFER",
                source.getAccountNumber(), destination.getAccountNumber(), "SUCCESS");
        transactionService.recordTransaction(tx, processedRefIds, transactionHistory);
        source.processTransaction(tx);
        return "SUCCESS";
    }

    public double checkBalance(Account account, String pin) {
        if (!validatePin(account, pin)) {
            return -1.0;
        }
        if (account.isLocked()) {
            return -2.0;
        }
        return account.getBalance();
    }
}