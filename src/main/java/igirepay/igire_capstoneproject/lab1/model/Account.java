
package igirepay.igire_capstoneproject.lab1.model;

import java.time.LocalDate;
import igirepay.igire_capstoneproject.lab1.model.Transaction;

public abstract class Account {
    private String accountNumber;
    private String pin;
    private double balance;
    private String accountHolderName;
    private boolean locked;
    private int pinAttempts;
    private double dailyWithdrawnAmount;
    private LocalDate lastWithdrawalDate;

    public Account(String accountNumber, String pin, String accountHolderName) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.accountHolderName = accountHolderName;
        this.balance = 0.0;
        this.locked = false;
        this.pinAttempts = 0;
        this.dailyWithdrawnAmount = 0.0;
        this.lastWithdrawalDate = LocalDate.now();
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public abstract void processTransaction(Transaction transaction);

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public int getPinAttempts() { return pinAttempts; }
    public void setPinAttempts(int pinAttempts) { this.pinAttempts = pinAttempts; }
    public double getDailyWithdrawnAmount() { return dailyWithdrawnAmount; }
    public void setDailyWithdrawnAmount(double dailyWithdrawnAmount) { this.dailyWithdrawnAmount = dailyWithdrawnAmount; }
    public LocalDate getLastWithdrawalDate() { return lastWithdrawalDate; }
    public void setLastWithdrawalDate(LocalDate lastWithdrawalDate) { this.lastWithdrawalDate = lastWithdrawalDate; }
}