package io.github.jhanvi857.nioflow.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    void hash_plaintext_returnsBcryptHash() {
        String hash = PasswordHasher.hash("password");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
        assertEquals(60, hash.length());
    }

    @Test
    void hash_sameInput_differentOutputEachTime() {
        String hash1 = PasswordHasher.hash("password");
        String hash2 = PasswordHasher.hash("password");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void verify_correctPassword_returnsTrue() {
        String password = "secret-password";
        String hash = PasswordHasher.hash(password);
        assertTrue(PasswordHasher.verify(password, hash));
    }

    @Test
    void verify_wrongPassword_returnsFalse() {
        String hash = PasswordHasher.hash("secret");
        assertFalse(PasswordHasher.verify("wrong", hash));
    }

    @Test
    void verify_emptyPassword_doesNotThrow() {
        String hash = PasswordHasher.hash("something");
        assertDoesNotThrow(() -> assertFalse(PasswordHasher.verify("", hash)));
    }

    @Test
    void hash_emptyString_returnsValidHash() {
        String hash = PasswordHasher.hash("");
        assertNotNull(hash);
        assertTrue(PasswordHasher.verify("", hash));
    }

    @Test
    void verify_nullPassword_handledGracefully() {
        String hash = PasswordHasher.hash("test");
        assertFalse(PasswordHasher.verify(null, hash));
        assertFalse(PasswordHasher.verify("test", null));
    }

    @Test
    void hash_null_returnsNull() {
        assertNull(PasswordHasher.hash(null));
    }
}
