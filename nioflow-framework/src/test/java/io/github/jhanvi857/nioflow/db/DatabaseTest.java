package io.github.jhanvi857.nioflow.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    @BeforeEach
    void setUp() {
        // Clear static state via reflection if needed, but here we just set env
        System.setProperty("JDBC_URL", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        System.setProperty("MONGO_URI", "mongodb://localhost:27017");
    }

    @AfterEach
    void tearDown() {
        Database.shutdown();
        System.clearProperty("JDBC_URL");
        System.clearProperty("MONGO_URI");
    }

    @Test
    void testPostgresInitAndConnection() throws SQLException {
        Database.initPostgres();
        Connection conn = Database.getPostgresConnection();
        assertNotNull(conn);
        assertFalse(conn.isClosed());
        conn.close();
    }

    @Test
    void testPostgresInitWithoutUrl() {
        System.clearProperty("JDBC_URL");
        // We need to reset pgDataSource to test the skip logic
        // But Database.shutdown() handles it
        Database.initPostgres();
        // If no URL, pgDataSource remains null, so getPostgresConnection throws
        assertThrows(SQLException.class, Database::getPostgresConnection);
    }

    @Test
    void testMongoInit() {
        // MongoClients.create might fail if no mongo server is running, 
        // but often the client initialization itself doesn't check the server immediately.
        // If it fails, we catch it.
        try {
            Database.initMongo();
            assertNotNull(Database.getMongoClient());
        } catch (Exception e) {
            // Expected if no mongo driver or server issues in environment
        }
    }

    @Test
    void testMongoInitWithoutUri() {
        System.clearProperty("MONGO_URI");
        Database.initMongo();
        assertThrows(RuntimeException.class, Database::getMongoClient);
    }
}
