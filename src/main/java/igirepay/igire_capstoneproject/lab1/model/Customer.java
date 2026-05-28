// ===== File: Customer.java =====
package igirepay.igire_capstoneproject.lab1.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String customerId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private List<Account> accounts;

    public Customer(String customerId, String fullName, String email, String phoneNumber) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    // Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    @Override
    public String toString() {
        return "Customer{id='" + customerId + "', name='" + fullName + "', email='" + email + "'}";
    }
}