package igirepay.igire_capstoneproject.lab2.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab2.config.DatabaseConnection;
import igirepay.igire_capstoneproject.lab2.dao.ProcessedRequestDAO;

public class ProcessedRequestDAOImpl implements ProcessedRequestDAO {

    @Override
    public boolean existsByReferenceId(UUID referenceId) {
        final String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, referenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check processed request", e);
        }
    }

    @Override
    public void create(UUID referenceId, Timestamp processedAt) {
        final String sql = "INSERT INTO processed_requests (id, reference_id, processed_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, referenceId);
            ps.setTimestamp(3, processedAt);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to persist processed request", e);
        }
    }
}

