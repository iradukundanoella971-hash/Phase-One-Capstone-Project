package igirepay.igire_capstoneproject.lab2.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String DB_NAME = "IGIREPAY";
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5433/" + DB_NAME;
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "123";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        // PreparedStatement-only rule is enforced in DAO layer; here we just provide connections.
        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }
}

