package igirepay.igire_capstoneproject.lab2.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab2.config.DatabaseConnection;
import igirepay.igire_capstoneproject.lab2.dao.CustomerDAO;

public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public void create(Customer customer) {
        final String sql = "INSERT INTO customers (id, full_name, email, phone_number) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, customer.getCustomerId());
            ps.setString(2, customer.getFullName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhoneNumber());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create customer", e);
        }
    }

    @Override
    public Customer findById(UUID id) {
        final String sql = "SELECT id, full_name, email, phone_number FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Customer customer = new Customer(
                        rs.getObject("id", UUID.class),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone_number")
                );
                return customer;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by id", e);
        }
    }

    @Override
    public List<Customer> findAll() {
        final String sql = "SELECT id, full_name, email, phone_number FROM customers";

        List<Customer> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getObject("id", UUID.class),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone_number")
                );
                result.add(customer);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch customers", e);
        }

        return result;
    }

    @Override
    public void update(Customer customer) {
        final String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhoneNumber());
            ps.setObject(4, customer.getCustomerId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update customer", e);
        }
    }

    @Override
    public void delete(UUID id) {
        final String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete customer", e);
        }
    }
}

