package com.jhanvi857.coreHTTP.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    public static boolean isEnabled() {
        String enabled = System.getProperty("corehttp.enableDB");
        if (enabled == null || enabled.isBlank()) {
            enabled = System.getenv("COREHTTP_ENABLE_DB");
        }
        return "true".equalsIgnoreCase(enabled);
    }

    static {
        if (!isEnabled()) {
            logger.info(
                    "Database is disabled by default. Set COREHTTP_ENABLE_DB=true or -Dcorehttp.enableDB=true to enable.");
        } else {
            try {
                HikariConfig config = new HikariConfig();

                // Env-based configuration
                String jdbcUrl = System.getenv("JDBC_URL");
                if (jdbcUrl == null)
                    jdbcUrl = "jdbc:postgresql://localhost:5432/corehttp";

                String user = System.getenv("DB_USER");
                if (user == null)
                    user = "postgres";

                String pass = System.getenv("DB_PASS");
                if (pass == null || pass.isBlank()) {
                    logger.error("DB_PASS environment variable is required when database is enabled.");
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
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!isEnabled()) {
            throw new SQLException("Database is disabled. Set COREHTTP_ENABLE_DB=true to enable.");
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
}
