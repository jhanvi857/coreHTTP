package io.github.jhanvi857.nioflow.db;

import io.github.jhanvi857.nioflow.Env;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);
    private static HikariDataSource pgDataSource;
    private static MongoClient mongoClient;

    private static CircuitBreaker pgCircuitBreaker;
    private static Retry pgRetry;

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
            config.setMaximumPoolSize(Env.getAsInt("DB_POOL_MAX", 20));
            config.setMinimumIdle(Env.getAsInt("DB_POOL_MIN", 5));
            config.setConnectionTimeout(5000);
            pgDataSource = new HikariDataSource(config);

            CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofSeconds(10))
                    .permittedNumberOfCallsInHalfOpenState(5)
                    .slidingWindowSize(20)
                    .build();
            pgCircuitBreaker = CircuitBreaker.of("postgres-cb", cbConfig);

            RetryConfig retryConfig = RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(500))
                    .retryExceptions(SQLException.class)
                    .build();
            pgRetry = Retry.of("postgres-retry", retryConfig);

            logger.info("Successfully initialized PostgreSQL connection pool with Resiliency (CB + Retry).");
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

    /**
     * Gets a connection with Circuit Breaker and Retry logic already applied.
     */
    public static Connection getPostgresConnection() throws SQLException {
        if (pgDataSource == null) {
            initPostgres();
        }
        if (pgDataSource == null) {
            throw new SQLException("PostgreSQL Data Source is not initialized.");
        }

        try {
            return Retry.decorateCheckedSupplier(pgRetry,
                    CircuitBreaker.decorateCheckedSupplier(pgCircuitBreaker, () -> pgDataSource.getConnection())).get();
        } catch (Throwable t) {
            if (t instanceof SQLException)
                throw (SQLException) t;
            throw new SQLException("Failed to acquire connection due to circuit breaker or retry exhaustion", t);
        }
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
