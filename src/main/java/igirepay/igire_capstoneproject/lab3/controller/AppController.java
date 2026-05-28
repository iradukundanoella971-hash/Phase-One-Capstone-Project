package igirepay.igire_capstoneproject.lab3.controller;

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
import igirepay.igire_capstoneproject.lab2.service.TransactionService;
import igirepay.igire_capstoneproject.lab3.util.TransactionHistoryLoader;

import java.util.List;
import java.util.UUID;

public class AppController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AccountDAO accountDAO;

    public AppController() {
        this.customerService = new CustomerService();
        this.accountService = new AccountService();
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

    public String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
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
}
