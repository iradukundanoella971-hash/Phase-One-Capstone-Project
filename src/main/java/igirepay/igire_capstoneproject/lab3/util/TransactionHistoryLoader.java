package igirepay.igire_capstoneproject.lab3.util;

import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab2.config.DatabaseConnection;
import igirepay.igire_capstoneproject.lab2.dao.TransactionDAO;
import igirepay.igire_capstoneproject.lab2.dao.impl.TransactionDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TransactionHistoryLoader {

    private TransactionHistoryLoader() {
    }

    public static List<Transaction> loadForAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return List.of();
        }

        final String sql = """
                SELECT reference_id, amount, fee, transaction_type, source_account_number,
                       target_account_number, status, failure_reason, created_at
                FROM transactions
                WHERE source_account_number = ? OR target_account_number = ?
                ORDER BY created_at DESC
                """;

        List<Transaction> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber.trim());
            ps.setString(2, accountNumber.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        tx.setTimestamp(createdAt.toLocalDateTime());
                    }
                    result.add(tx);
                }
            }
            return result;
        } catch (SQLException e) {
            TransactionDAO dao = new TransactionDAOImpl();
            return dao.findAll().stream()
                    .filter(tx -> accountNumber.equals(tx.getSourceAccountNumber())
                            || accountNumber.equals(tx.getTargetAccountNumber()))
                    .toList();
        }
    }
}
