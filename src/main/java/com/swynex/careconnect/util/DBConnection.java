package com.swynex.careconnect.util;

import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnection {
    private final DatabaseConfig configuration;

    public DBConnection(DatabaseConfig configuration) {
        this.configuration = configuration;
    }

    public static DBConnection fromEnvironment() {
        return new DBConnection(DatabaseConfig.fromEnvironment());
    }

    public Connection getConnection() throws SQLException {
        return configuration.getConnection();
    }
}
