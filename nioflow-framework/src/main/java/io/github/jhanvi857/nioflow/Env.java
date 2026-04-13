package io.github.jhanvi857.nioflow;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Env {
    private static final Logger logger = LoggerFactory.getLogger(Env.class);
    private static Dotenv dotenv;

    static {
        load();
    }

    public static void load() {
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            logger.info("Environment variables loaded from .env file (if present).");
        } catch (Exception e) {
            logger.warn("Could not load .env file: {}", e.getMessage());
        }
    }

    /**
     * Gets an environment variable or system property.
     * Looks in:
     * 1. System properties (-Dkey=value)
     * 2. Loaded .env file
     * 3. OS environment variables
     *
     * @param key the key to search for
     * @return the value, or null if not found
     */
    public static String get(String key) {
        String val = System.getProperty(key);
        if (val != null && !val.isBlank())
            return val;

        if (dotenv != null) {
            val = dotenv.get(key);
            if (val != null && !val.isBlank())
                return val;
        }
        return System.getenv(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    public static boolean getAsBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null || val.isBlank())
            return defaultValue;
        return Boolean.parseBoolean(val);
    }

    public static int getAsInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null || val.isBlank())
            return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            logger.warn("Environment variable {} is not a valid integer: {}", key, val);
            return defaultValue;
        }
    }
}
