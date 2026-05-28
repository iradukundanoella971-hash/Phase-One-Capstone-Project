
package igirepay.igire_capstoneproject.lab1;

import igirepay.igire_capstoneproject.lab1.model.*;
import igirepay.igire_capstoneproject.lab1.service.*;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Shared collections
        List<Customer> customers = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        List<Transaction> transactionHistory = new ArrayList<>();
        Set<String> processedRefIds = new HashSet<>();
        Map<String, String> failedTxLogs = new HashMap<>();

        // Services
        TransactionService transactionService = new TransactionService();
        AccountService accountService = new AccountService(accounts, transactionHistory,
                processedRefIds, failedTxLogs, transactionService);
        CustomerService customerService = new CustomerService(customers);

        boolean running = true;

        while (running) {
            System.out.println("\n===== IgirePay Payment Gateway =====");
            System.out.println("1. Register Customer");
            System.out.println("2. Create Wallet Account");
            System.out.println("3. Create Savings Account");
            System.out.println("4. Deposit Money");
            System.out.println("5. Withdraw Money");
            System.out.println("6. Transfer Money");
            System.out.println("7. Check Balance");
            System.out.println("8. View Transaction History");
            System.out.println("9. View Failed Transactions");
            System.out.println("10. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Full Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Phone (10-12 digits): ");
                    String phone = scanner.nextLine();
                    String result = customerService.registerCustomer(name, email, phone);
                    if (result.startsWith("CUST-")) {
                        System.out.println("Customer registered successfully. ID: " + result);
                    } else {
                        System.out.println("Registration failed: " + result);
                    }
                    break;

                case 2:
                    System.out.print("Enter Customer ID: ");
                    String custId = scanner.nextLine();
                    Customer cust = customerService.findCustomerById(custId);
                    if (cust == null) {
                        System.out.println("Customer not found.");
                        break;
                    }
                    System.out.print("Set 5-digit PIN: ");
                    String pin = scanner.nextLine();
                    if (!igirepay.igire_capstoneproject.lab1.util.ValidationUtils.isValidPin(pin)) {
                        System.out.println("Invalid PIN. Must be 5 digits.");
                        break;
                    }
                    String accNum = igirepay.igire_capstoneproject.lab1.util.IdGenerator.generateAccountNumber();
                    WalletAccount wallet = new WalletAccount(accNum, pin, cust.getFullName());
                    accounts.add(wallet);
                    customerService.addAccountToCustomer(cust, wallet);
                    System.out.println("Wallet Account created. Number: " + accNum);
                    break;

                case 3:
                    System.out.print("Enter Customer ID: ");
                    custId = scanner.nextLine();
                    cust = customerService.findCustomerById(custId);
                    if (cust == null) {
                        System.out.println("Customer not found.");
                        break;
                    }
                    System.out.print("Set 5-digit PIN: ");
                    pin = scanner.nextLine();
                    if (!igirepay.igire_capstoneproject.lab1.util.ValidationUtils.isValidPin(pin)) {
                        System.out.println("Invalid PIN. Must be 5 digits.");
                        break;
                    }
                    accNum = igirepay.igire_capstoneproject.lab1.util.IdGenerator.generateAccountNumber();
                    SavingsAccount savings = new SavingsAccount(accNum, pin, cust.getFullName());
                    accounts.add(savings);
                    customerService.addAccountToCustomer(cust, savings);
                    System.out.println("Savings Account created. Number: " + accNum);
                    break;

                case 4:
                    System.out.print("Account Number: ");
                    String accNumDep = scanner.nextLine();
                    Account depAcc = accountService.findAccountByNumber(accNumDep);
                    if (depAcc == null) {
                        System.out.println("Account not found.");
                        break;
                    }
                    System.out.print("Amount to deposit: ");
                    double amountDep = scanner.nextDouble();
                    scanner.nextLine();
                    String refId = transactionService.generateUniqueReferenceId(processedRefIds);
                    String status = accountService.deposit(depAcc, amountDep, refId);
                    if (status.equals("SUCCESS")) {
                        System.out.println("Deposit successful. New balance: " + depAcc.getBalance());
                    } else {
                        System.out.println("Deposit failed: " + status);
                        transactionService.logFailedTransaction(refId, status, failedTxLogs);
                    }
                    break;

                case 5:
                    System.out.print("Account Number: ");
                    String accNumWith = scanner.nextLine();
                    Account withAcc = accountService.findAccountByNumber(accNumWith);
                    if (withAcc == null) {
                        System.out.println("Account not found.");
                        break;
                    }
                    System.out.print("PIN: ");
                    String pinWith = scanner.nextLine();
                    System.out.print("Amount to withdraw: ");
                    double amountWith = scanner.nextDouble();
                    scanner.nextLine();
                    refId = transactionService.generateUniqueReferenceId(processedRefIds);
                    status = accountService.withdraw(withAcc, pinWith, amountWith, refId);
                    if (status.equals("SUCCESS")) {
                        System.out.println("Withdrawal successful. New balance: " + withAcc.getBalance());
                    } else {
                        System.out.println("Withdrawal failed: " + status);
                        transactionService.logFailedTransaction(refId, status, failedTxLogs);
                    }
                    break;

                case 6:
                    System.out.print("Source Account Number: ");
                    String srcNum = scanner.nextLine();
                    Account srcAcc = accountService.findAccountByNumber(srcNum);
                    if (srcAcc == null) {
                        System.out.println("Source account not found.");
                        break;
                    }
                    System.out.print("Source PIN: ");
                    String srcPin = scanner.nextLine();
                    System.out.print("Destination Account Number: ");
                    String destNum = scanner.nextLine();
                    Account destAcc = accountService.findAccountByNumber(destNum);
                    if (destAcc == null) {
                        System.out.println("Destination account not found.");
                        break;
                    }
                    System.out.print("Amount to transfer: ");
                    double amountTransfer = scanner.nextDouble();
                    scanner.nextLine();
                    refId = transactionService.generateUniqueReferenceId(processedRefIds);
                    status = accountService.transfer(srcAcc, srcPin, destAcc, amountTransfer, refId);
                    if (status.equals("SUCCESS")) {
                        System.out.println("Transfer successful.");
                    } else {
                        System.out.println("Transfer failed: " + status);
                        transactionService.logFailedTransaction(refId, status, failedTxLogs);
                    }
                    break;

                case 7:
                    System.out.print("Account Number: ");
                    String balAccNum = scanner.nextLine();
                    Account balAcc = accountService.findAccountByNumber(balAccNum);
                    if (balAcc == null) {
                        System.out.println("Account not found.");
                        break;
                    }
                    System.out.print("PIN: ");
                    String pinBal = scanner.nextLine();
                    double balance = accountService.checkBalance(balAcc, pinBal);
                    if (balance == -1.0) {
                        System.out.println("Invalid PIN.");
                    } else if (balance == -2.0) {
                        System.out.println("Account locked.");
                    } else {
                        System.out.println("Current balance: " + balance + " RWF");
                    }
                    break;

                case 8:
                    transactionService.displayTransactionHistory(transactionHistory);
                    break;

                case 9:
                    transactionService.displayFailedTransactions(failedTxLogs);
                    break;

                case 10:
                    System.out.println("Exiting IgirePay. Thank you!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please choose 1-10.");
            }
        }
        scanner.close();
    }
}