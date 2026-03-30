package com.jhanvi857.nioflow.db;

import com.jhanvi857.nioflow.Env;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);
    private static HikariDataSource pgDataSource;
    private static MongoClient mongoClient;

    public static void initPostgres() {
        if (pgDataSource != null)
            return;

        try {
            String url = Env.get("JDBC_URL");
            String user = Env.get("DB_USER", "postgres");
            String pass = Env.get("DB_PASS");

            if (url == null || url.isBlank()) {
                logger.warn("JDBC_URL not found in environment. PostgreSQL initialization skipped.");
                return;
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            config.setMaximumPoolSize(Env.getAsInt("DB_POOL_MAX", 10));
            config.setMinimumIdle(Env.getAsInt("DB_POOL_MIN", 2));
            config.setConnectionTimeout(20000);
            pgDataSource = new HikariDataSource(config);
            logger.info("Successfully initialized PostgreSQL connection pool.");
        } catch (Exception e) {
            logger.error("Failed to initialize PostgreSQL: {}", e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static void initMongo() {
        if (mongoClient != null)
            return;

        try {
            String uri = Env.get("MONGO_URI");
            if (uri == null || uri.isBlank()) {
                logger.warn("MONGO_URI not found in environment. MongoDB initialization skipped.");
                return;
            }

            mongoClient = MongoClients.create(uri);
            logger.info("Successfully initialized MongoDB client.");
        } catch (Exception e) {
            logger.error("Failed to initialize MongoDB: {}", e.getMessage());
            throw new RuntimeException("MongoDB initialization failed", e);
        }
    }

    public static Connection getPostgresConnection() throws SQLException {
        if (pgDataSource == null) {
            initPostgres();
        }
        if (pgDataSource == null) {
            throw new SQLException("PostgreSQL Data Source is not initialized.");
        }
        return pgDataSource.getConnection();
    }

    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            initMongo();
        }
        if (mongoClient == null) {
            throw new RuntimeException("MongoDB Client is not initialized.");
        }
        return mongoClient;
    }

    public static void shutdown() {
        if (pgDataSource != null) {
            pgDataSource.close();
            logger.info("PostgreSQL connection pool closed.");
        }
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB client closed.");
        }
    }
}
