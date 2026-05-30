package igirepay.igire_capstoneproject.lab2.dao.impl;

import igirepay.igire_capstoneproject.lab2.config.DatabaseConnection;
import igirepay.igire_capstoneproject.lab2.dao.LoanDAO;
import igirepay.igire_capstoneproject.lab2.model.Loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoanDAOImpl implements LoanDAO {

    public LoanDAOImpl() {
        ensureTableExists();
    }

    private void ensureTableExists() {
        final String sql = """
                CREATE TABLE IF NOT EXISTS loans (
                    loan_id UUID PRIMARY KEY,
                    user_id UUID NOT NULL,
                    loan_amount NUMERIC(18, 2) NOT NULL,
                    issue_date DATE NOT NULL,
                    due_date DATE NOT NULL,
                    repayment_status VARCHAR(20) NOT NULL,
                    status VARCHAR(20) NOT NULL
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize loans table", e);
        }
    }

    @Override
    public void create(Loan loan) {
        final String sql = "INSERT INTO loans (loan_id, user_id, loan_amount, issue_date, due_date, repayment_status, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, loan.getLoanId());
            ps.setObject(2, loan.getUserId());
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(loan.getLoanAmount()));
            ps.setDate(4, Date.valueOf(loan.getIssueDate()));
            ps.setDate(5, Date.valueOf(loan.getDueDate()));
            ps.setString(6, loan.getRepaymentStatus());
            ps.setString(7, loan.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create loan", e);
        }
    }

    @Override
    public Loan findById(UUID id) {
        final String sql = "SELECT * FROM loans WHERE loan_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapLoan(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find loan by id", e);
        }
    }

    @Override
    public Loan findOutstandingByUserId(UUID userId) {
        final String sql = """
                SELECT * FROM loans
                WHERE user_id = ? AND repayment_status <> 'PAID'
                ORDER BY issue_date DESC, loan_id DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapLoan(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find outstanding loan", e);
        }
    }

    @Override
    public List<Loan> findByUserId(UUID userId) {
        final String sql = "SELECT * FROM loans WHERE user_id = ? ORDER BY issue_date DESC, loan_id DESC";
        List<Loan> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapLoan(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list loans for user", e);
        }

        return result;
    }

    @Override
    public List<Loan> findAll() {
        final String sql = "SELECT * FROM loans ORDER BY issue_date DESC, loan_id DESC";
        List<Loan> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapLoan(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch loans", e);
        }

        return result;
    }

    @Override
    public void update(Loan loan) {
        final String sql = """
                UPDATE loans
                SET user_id = ?, loan_amount = ?, issue_date = ?, due_date = ?, repayment_status = ?, status = ?
                WHERE loan_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, loan.getUserId());
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(loan.getLoanAmount()));
            ps.setDate(3, Date.valueOf(loan.getIssueDate()));
            ps.setDate(4, Date.valueOf(loan.getDueDate()));
            ps.setString(5, loan.getRepaymentStatus());
            ps.setString(6, loan.getStatus());
            ps.setObject(7, loan.getLoanId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update loan", e);
        }
    }

    private Loan mapLoan(ResultSet rs) throws SQLException {
        return new Loan(
                rs.getObject("loan_id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getBigDecimal("loan_amount").doubleValue(),
                rs.getDate("issue_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                rs.getString("repayment_status"),
                rs.getString("status")
        );
    }
}
