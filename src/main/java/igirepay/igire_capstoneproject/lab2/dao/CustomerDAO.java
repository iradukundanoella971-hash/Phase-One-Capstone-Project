package igirepay.igire_capstoneproject.lab2.dao;

import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Customer;

public interface CustomerDAO {
    void create(Customer customer);
    Customer findById(UUID id);
    List<Customer> findAll();
    void update(Customer customer);
    void delete(UUID id);
}

