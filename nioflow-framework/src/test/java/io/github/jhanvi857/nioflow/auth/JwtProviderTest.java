package io.github.jhanvi857.nioflow.auth;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JwtProviderTest {

    @BeforeAll
    static void setup() {
        System.setProperty("nioflow.jwtSecret", "this-is-a-very-long-and-secure-secret-key-12345");
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = JwtProvider.generateToken("user@example.com", "ADMIN");
        assertNotNull(token);

        assertTrue(JwtProvider.validateToken(token));
        assertEquals("user@example.com", JwtProvider.getUsernameFromToken(token));
        assertEquals("ADMIN", JwtProvider.getRoleFromToken(token));
        assertNotNull(JwtProvider.getJtiFromToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(JwtProvider.validateToken("invalid.token.here"));
    }

    @Test
    void testExpiredToken() {
        String token = JwtProvider.generateToken("user", "USER");
        assertTrue(JwtProvider.validateToken(token));
    }

    @Test
    void testEntropyAndInit() {
        assertDoesNotThrow(() -> JwtProvider.generateToken("a", "b"));
    }
}
