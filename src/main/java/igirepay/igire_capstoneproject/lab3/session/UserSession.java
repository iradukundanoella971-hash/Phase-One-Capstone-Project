package igirepay.igire_capstoneproject.lab3.session;

import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Customer;

public class UserSession {

    private Customer customer;
    private Account activeAccount;
    private String pin;

    public boolean isLoggedIn() {
        return customer != null && activeAccount != null && pin != null;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Account getActiveAccount() {
        return activeAccount;
    }

    public void setActiveAccount(Account activeAccount) {
        this.activeAccount = activeAccount;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void clear() {
        customer = null;
        activeAccount = null;
        pin = null;
    }
}
