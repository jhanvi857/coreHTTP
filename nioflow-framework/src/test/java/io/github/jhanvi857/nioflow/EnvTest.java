package io.github.jhanvi857.nioflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvTest {

    @BeforeEach
    public void setUp() {
        System.clearProperty("TEST_KEY");
        System.clearProperty("TEST_INT");
        System.clearProperty("TEST_BOOL");
    }

    @AfterEach
    public void tearDown() {
        System.clearProperty("TEST_KEY");
        System.clearProperty("TEST_INT");
        System.clearProperty("TEST_BOOL");
    }

    @Test
    public void get_existingKey_returnsValue() {
        System.setProperty("TEST_KEY", "value123");
        assertEquals("value123", Env.get("TEST_KEY"));
    }

    @Test
    public void get_missingKey_returnsNull() {
        assertNull(Env.get("NON_EXISTENT_KEY_999"));
    }

    @Test
    public void getOrDefault_missingKey_returnsDefault() {
        assertEquals("default", Env.get("MISSING_KEY", "default"));
    }

    @Test
    public void getOrDefault_presentKey_returnsValue() {
        System.setProperty("TEST_KEY", "actual");
        assertEquals("actual", Env.get("TEST_KEY", "default"));
    }

    @Test
    public void getAsInt_validValue_returnsInteger() {
        System.setProperty("TEST_INT", "42");
        assertEquals(42, Env.getAsInt("TEST_INT", 0));
    }

    @Test
    public void getAsInt_invalidValue_returnsDefault() {
        System.setProperty("TEST_INT", "not-an-int");
        assertEquals(100, Env.getAsInt("TEST_INT", 100));
    }

    @Test
    public void getAsInt_missingKey_returnsDefault() {
        assertEquals(500, Env.getAsInt("MISSING_INT", 500));
    }

    @Test
    public void getAsBoolean_true_returnsTrue() {
        System.setProperty("TEST_BOOL", "true");
        assertTrue(Env.getAsBoolean("TEST_BOOL", false));
    }

    @Test
    public void getAsBoolean_false_returnsFalse() {
        System.setProperty("TEST_BOOL", "false");
        assertFalse(Env.getAsBoolean("TEST_BOOL", true));
    }

    @Test
    public void getAsBoolean_missing_returnsDefault() {
        assertTrue(Env.getAsBoolean("MISSING_BOOL", true));
        assertFalse(Env.getAsBoolean("MISSING_BOOL", false));
    }

    @Test
    public void load_doesNotThrow() {
        assertDoesNotThrow(Env::load);
    }
}
