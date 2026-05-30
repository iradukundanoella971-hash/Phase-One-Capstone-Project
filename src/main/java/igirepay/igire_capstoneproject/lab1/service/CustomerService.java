
package igirepay.igire_capstoneproject.lab1.service;

import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab1.util.ValidationUtils;

public class CustomerService {
    private List<Customer> customers;

    public CustomerService(List<Customer> customers) {
        this.customers = customers;
    }

    public String registerCustomer(String fullName, String email, String phone) {
        if (!ValidationUtils.isValidEmail(email)) {
            return "Invalid email format";
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            return "Invalid phone number (10-12 digits)";
        }
        UUID customerId = UUID.randomUUID();
        Customer newCustomer = new Customer(customerId, fullName, email, phone);
        customers.add(newCustomer);
        return customerId.toString();
    }

    public Customer findCustomerById(String customerId) {
        for (Customer c : customers) {
            if (c.getCustomerId().toString().equals(customerId)) {
                return c;
            }
        }
        return null;
    }

    public void addAccountToCustomer(Customer customer, Account account) {
        if (customer != null && account != null) {
            customer.addAccount(account);
        }
    }

    public void displayAllCustomers() {
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        for (Customer c : customers) {
            System.out.println(c);
        }
    }
}