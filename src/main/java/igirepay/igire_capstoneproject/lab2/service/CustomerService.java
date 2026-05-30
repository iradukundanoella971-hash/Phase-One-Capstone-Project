package igirepay.igire_capstoneproject.lab2.service;

import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab1.util.ValidationUtils;
import igirepay.igire_capstoneproject.lab2.dao.CustomerDAO;
import igirepay.igire_capstoneproject.lab2.dao.impl.CustomerDAOImpl;

public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAOImpl();
    }

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public String registerCustomer(String fullName, String email, String phoneNumber) {
        if (fullName == null || fullName.isBlank()) {
            return "Invalid full name";
        }
        if (!ValidationUtils.isValidEmail(email)) {
            return "Invalid email";
        }
        if (!ValidationUtils.isValidPhone(phoneNumber)) {
            return "Invalid phone number";
        }

        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, fullName.trim(), email.trim(), phoneNumber.trim());
        customerDAO.create(customer);
        return id.toString();
    }

    public Customer findCustomerById(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return null;
        }
        try {
            return customerDAO.findById(UUID.fromString(customerId.trim()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Customer findCustomerByPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return customerDAO.findByPhoneNumber(phoneNumber.trim());
    }

    public List<Customer> listCustomers() {
        return customerDAO.findAll();
    }

    public boolean updateCustomer(String customerId, String fullName, String email, String phoneNumber) {
        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            return false;
        }

        if (fullName != null && !fullName.isBlank()) {
            customer.setFullName(fullName.trim());
        }
        if (email != null && !email.isBlank()) {
            if (!ValidationUtils.isValidEmail(email)) {
                return false;
            }
            customer.setEmail(email.trim());
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            if (!ValidationUtils.isValidPhone(phoneNumber)) {
                return false;
            }
            customer.setPhoneNumber(phoneNumber.trim());
        }

        customerDAO.update(customer);
        return true;
    }

    public void deleteCustomer(String customerId) {
        try {
            customerDAO.delete(UUID.fromString(customerId.trim()));
        } catch (IllegalArgumentException ignored) {
            // no-op
        }
    }
}

