package igirepay.igire_capstoneproject.lab2.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab2.config.DatabaseConnection;
import igirepay.igire_capstoneproject.lab2.dao.TransactionDAO;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public void create(Transaction transaction) {
        // Transaction model does not have account UUID; it only has account numbers.
        // We use account_number fields to resolve account_id in SQL.
        final String sql = "INSERT INTO transactions (id, account_id, reference_id, transaction_type, amount, fee, source_account_number, target_account_number, status, failure_reason) " +
                "SELECT ?, a.id, ?, ?, ?, ?, ?, ?, ?, ? " +
                "FROM accounts a WHERE a.account_number = ?";

        // Note: transaction_type -> transaction.getType()
        UUID id = UUID.randomUUID();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.setObject(2, transaction.getReferenceId());
            ps.setString(3, transaction.getType());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(transaction.getAmount()));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(transaction.getFee()));
            ps.setString(6, transaction.getSourceAccountNumber());
            ps.setString(7, transaction.getTargetAccountNumber());
            ps.setString(8, transaction.getStatus());
            ps.setString(9, transaction.getFailureReason());
            ps.setString(10, transaction.getSourceAccountNumber());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create transaction", e);
        }
    }

    @Override
    public Transaction findById(UUID id) {
        final String sql = "SELECT * FROM transactions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapTransaction(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transaction by id", e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        final String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        List<Transaction> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapTransaction(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch transactions", e);
        }

        return result;
    }

    @Override
    public void update(Transaction transaction) {
        // Update by reference_id.
        final String sql = "UPDATE transactions SET transaction_type = ?, amount = ?, fee = ?, status = ?, failure_reason = ? WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transaction.getType());
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(transaction.getAmount()));
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(transaction.getFee()));
            ps.setString(4, transaction.getStatus());
            ps.setString(5, transaction.getFailureReason());
            ps.setObject(6, transaction.getReferenceId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    @Override
    public void delete(UUID id) {
        final String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    @Override
    public List<Transaction> findByAccountId(UUID accountId) {
        final String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        List<Transaction> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapTransaction(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch transactions by accountId", e);
        }

        return result;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction(
                rs.getObject("reference_id", UUID.class),
                rs.getBigDecimal("amount").doubleValue(),
                rs.getBigDecimal("fee").doubleValue(),
                rs.getString("transaction_type"),
                rs.getString("source_account_number"),
                rs.getString("target_account_number"),
                rs.getString("status")
        );
        tx.setFailureReason(rs.getString("failure_reason"));
        return tx;
    }
}

