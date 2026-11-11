package com.swynex.careconnect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {
    private static final String DATABASE_NAME = "careconnect_db";
    private static final String DEFAULT_URL =
            "jdbc:postgresql://localhost:5432/" + DATABASE_NAME;

    private final String url;
    private final String username;
    private final String password;

    private DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConfig fromEnvironment() {
    String url = System.getenv().getOrDefault("CARECONNECT_DB_URL", DEFAULT_URL);
    String username = System.getenv("CARECONNECT_DB_USERNAME");
    String password = System.getenv("CARECONNECT_DB_PASSWORD");

    System.out.println("===== DATABASE CONFIGURATION CHECK =====");
    System.out.println("Database URL configured: " + !url.isBlank());
    System.out.println("Username configured: "
            + (username != null && !username.isBlank()));
    System.out.println("Password configured: "
            + (password != null && !password.isBlank()));
    System.out.println("========================================");

    return new DatabaseConfig(url, username, password);
}

    public Connection getConnection() throws SQLException {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new SQLException(
                    "Database credentials are not configured. Set "
                            + "CARECONNECT_DB_USERNAME and CARECONNECT_DB_PASSWORD.");
        }
        Connection connection;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException exception) {
            throw new SQLException(
                    "Could not connect to PostgreSQL. Verify the server, URL, "
                            + "username, and password for database " + DATABASE_NAME + ".",
                    exception);
        }

        try {
            String actualDatabase = connection.getCatalog();
            if (!DATABASE_NAME.equals(actualDatabase)) {
                throw new SQLException(
                        "CareConnect must connect to database " + DATABASE_NAME
                                + ", but the configured URL selected "
                                + (actualDatabase == null ? "an unknown database"
                                        : actualDatabase) + ".");
            }
            return connection;
        } catch (SQLException exception) {
            try {
                connection.close();
            } catch (SQLException closeException) {
                exception.addSuppressed(closeException);
            }
            throw exception;
        }
    }
}
