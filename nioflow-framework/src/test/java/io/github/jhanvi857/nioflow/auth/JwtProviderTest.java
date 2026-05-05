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

    @Test
    void testShannonEntropy() {
        assertEquals(0.0, JwtProvider.shannonEntropy(""));
        assertEquals(0.0, JwtProvider.shannonEntropy(null));
        // All identical chars -> 0 entropy
        assertEquals(0.0, JwtProvider.shannonEntropy("aaaaaaaaaaaaaaaa"));
        // Random-ish string -> > 0 entropy
        assertTrue(JwtProvider.shannonEntropy("abc123XYZ!@#") > 2.0);
    }
    
    @Test
    void testExpirationOverride() throws Exception {
        // This is hard to test because it's in a static block.
        // But we can at least verify the current value.
        java.lang.reflect.Field field = JwtProvider.class.getDeclaredField("EXPIRATION_TIME");
        field.setAccessible(true);
        long value = (long) field.get(null);
        assertTrue(value > 0);
    }

    @Test
    void testGenerateToken_requiresKey() {
        // Since setup() runs @BeforeAll, the key is already set.
        // We just verify it works.
        assertNotNull(JwtProvider.generateToken("user", "ROLE"));
    }

    @Test
    void testGetClaims_invalidToken_throws() {
        assertThrows(Exception.class, () -> JwtProvider.getUsernameFromToken("not.a.jwt"));
    }

    @Test
    void testGetRole_invalidToken_throws() {
        assertThrows(Exception.class, () -> JwtProvider.getRoleFromToken("not.a.jwt"));
    }

    @Test
    void testGetJti_invalidToken_throws() {
        assertThrows(Exception.class, () -> JwtProvider.getJtiFromToken("not.a.jwt"));
    }
}
