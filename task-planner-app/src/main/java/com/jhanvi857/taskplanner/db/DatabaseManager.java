package com.jhanvi857.taskplanner.db;

import com.jhanvi857.nioflow.Env;
import com.jhanvi857.nioflow.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * App-specific DatabaseManager that delegates to the NioFlow framework's Database utility.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    public static boolean isEnabled() {
        return Env.getAsBoolean("NIOFLOW_ENABLE_DB", false)
                || Env.getAsBoolean("nioflow.enableDB", false);
    }

    static {
        if (isEnabled()) {
            try {
                // Initialize framework's postgres support
                Database.initPostgres();
                
                // If MongoDB is enabled in env, initialize it too
                if (Env.get("MONGO_URI") != null) {
                    Database.initMongo();
                }
                
                logger.info("Application database layer initialized via NioFlow framework.");
            } catch (Exception e) {
                logger.error("Failed to initialize database layer: {}", e.getMessage());
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        return Database.getPostgresConnection();
    }

    public static void shutdown() {
        Database.shutdown();
    }

    public static boolean isHealthy() {
        if (!isEnabled()) {
            return true;
        }
        try (Connection con = Database.getPostgresConnection()) {
            return con.isValid(2);
        } catch (SQLException e) {
            logger.warn("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
}
