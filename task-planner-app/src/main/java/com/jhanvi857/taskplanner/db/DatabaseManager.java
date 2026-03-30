package com.jhanvi857.taskplanner.db;

import com.jhanvi857.nioflow.Env;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    public static boolean isEnabled() {
        return Env.getAsBoolean("NIOFLOW_ENABLE_DB", false)
                || Env.getAsBoolean("nioflow.enableDB", false);
    }

    static {
        if (!isEnabled()) {
            logger.info(
                    "Database is disabled by default. Set NIOFLOW_ENABLE_DB=true or -Dnioflow.enableDB=true to enable via .env or JVM options.");
        } else {
            try {
                HikariConfig config = new HikariConfig();
                String jdbcUrl = Env.get("JDBC_URL",
                        Env.get("nioflow.jdbcUrl", "jdbc:postgresql://localhost:5432/nioflow"));
                String user = Env.get("DB_USER", Env.get("nioflow.dbUser", "postgres"));
                String pass = Env.get("DB_PASS", Env.get("nioflow.dbPass"));

                if (pass == null || pass.isBlank()) {
                    logger.error(
                            "DB_PASS (or nioflow.dbPass) is required when database is enabled. Please provide it via .env file or environment variable.");
                    throw new IllegalStateException("DB_PASS not configured.");
                }

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(user);
                config.setPassword(pass);

                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setIdleTimeout(300000);
                config.setConnectionTimeout(20000);

                // Optimization for PostgreSQL
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

                dataSource = new HikariDataSource(config);
                logger.info("Database connection pool initialized successfully.");
            } catch (Exception e) {
                logger.error("Failed to initialize database pool: {}", e.getMessage());
                throw new IllegalStateException("Database initialization failed while DB mode is enabled.", e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!isEnabled()) {
            throw new SQLException("Database is disabled. Set NIOFLOW_ENABLE_DB=true to enable.");
        }
        if (dataSource == null) {
            throw new SQLException("Data source not initialized");
        }
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static boolean isHealthy() {
        if (!isEnabled()) {
            return true;
        }
        if (dataSource == null) {
            return false;
        }

        try (Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT 1");
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 1;
        } catch (SQLException e) {
            logger.warn("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
}
