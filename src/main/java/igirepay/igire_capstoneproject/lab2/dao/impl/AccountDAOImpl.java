package igirepay.igire_capstoneproject.lab2.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.SavingsAccount;
import igirepay.igire_capstoneproject.lab1.model.WalletAccount;
import igirepay.igire_capstoneproject.lab2.config.DatabaseConnection;
import igirepay.igire_capstoneproject.lab2.dao.AccountDAO;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public void create(Account account, UUID customerId, String accountType) {
        // last_withdrawal_date should start as today.
        final String sql = "INSERT INTO accounts (id, customer_id, account_type, account_number, balance, pin, is_locked, pin_attempts, daily_withdrawn_amount, last_withdrawal_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        UUID id = UUID.randomUUID();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.setObject(2, customerId);
            ps.setString(3, accountType);
            ps.setString(4, account.getAccountNumber());
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(account.getBalance()));
            ps.setString(6, account.getPin());
            ps.setBoolean(7, account.isLocked());
            ps.setInt(8, account.getPinAttempts());
            ps.setBigDecimal(9, java.math.BigDecimal.valueOf(account.getDailyWithdrawnAmount()));
            ps.setDate(10, Date.valueOf(account.getLastWithdrawalDate()));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create account", e);
        }
    }

    @Override
    public Account findById(UUID id) {
        final String sql = "SELECT * FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapAccount(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account by id", e);
        }
    }

    @Override
    public List<Account> findAll() {
        final String sql = "SELECT * FROM accounts";
        List<Account> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapAccount(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch accounts", e);
        }

        return result;
    }

    @Override
    public void update(Account account) {
        // NOTE: We update by account_number because Account model does not carry UUID id.
        final String sql = "UPDATE accounts SET balance = ?, pin = ?, is_locked = ?, pin_attempts = ?, daily_withdrawn_amount = ?, last_withdrawal_date = ? " +
                "WHERE account_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, java.math.BigDecimal.valueOf(account.getBalance()));
            ps.setString(2, account.getPin());
            ps.setBoolean(3, account.isLocked());
            ps.setInt(4, account.getPinAttempts());
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(account.getDailyWithdrawnAmount()));
            ps.setDate(6, Date.valueOf(account.getLastWithdrawalDate()));
            ps.setString(7, account.getAccountNumber());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account", e);
        }
    }

    @Override
    public void delete(UUID id) {
        final String sql = "DELETE FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account", e);
        }
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        final String sql = "SELECT * FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapAccount(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account by account number", e);
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        String accountType = rs.getString("account_type");
        String accountNumber = rs.getString("account_number");
        String pin = rs.getString("pin");

        // Account model constructor expects holder name; we don't have it in schema.
        // We'll store customer_id as holder name placeholder.
        // Services can still operate correctly because they rely on balance/pin/lock.
        String holderPlaceholder = String.valueOf(rs.getObject("customer_id", UUID.class));

        Account account;
        if ("WALLET".equalsIgnoreCase(accountType)) {
            account = new WalletAccount(accountNumber, pin, holderPlaceholder);
        } else {
            account = new SavingsAccount(accountNumber, pin, holderPlaceholder);
        }

        account.setBalance(rs.getBigDecimal("balance").doubleValue());
        account.setLocked(rs.getBoolean("is_locked"));
        account.setPinAttempts(rs.getInt("pin_attempts"));
        account.setDailyWithdrawnAmount(rs.getBigDecimal("daily_withdrawn_amount").doubleValue());

        LocalDate lastDate = rs.getDate("last_withdrawal_date").toLocalDate();
        account.setLastWithdrawalDate(lastDate);

        return account;
    }
}

