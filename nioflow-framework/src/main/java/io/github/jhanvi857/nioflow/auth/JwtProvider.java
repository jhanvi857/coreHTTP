package io.github.jhanvi857.nioflow.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hardened JWT provider with issuer pinning, entropy validation,
 * JTI-based replay protection, and short-lived access tokens.
 */
public class JwtProvider {
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    /**
     * Default access token lifetime: 15 minutes.
     * Short-lived tokens limit the damage window of a stolen token.
     * Override with system property nioflow.jwt.expirationMs or env
     * NIOFLOW_JWT_EXPIRATION_MS.
     */
    private static final long EXPIRATION_TIME;
    private static final SecretKey SECRET_KEY;

    /** Fixed issuer claim — all tokens must match this value. */
    private static final String ISSUER = "nioflow";

    /** Minimum Shannon entropy bits required for the secret. */
    private static final double MIN_ENTROPY_BITS = 3.0;

    /** Minimum secret length (bytes). */
    private static final int MIN_SECRET_LENGTH = 32;

    static {
        // Resolve expiration time
        long expirationMs = 900_000L; // 15 min default
        String expirationOverride = System.getProperty("nioflow.jwt.expirationMs");
        if (expirationOverride == null || expirationOverride.isBlank()) {
            expirationOverride = System.getenv("NIOFLOW_JWT_EXPIRATION_MS");
        }
        if (expirationOverride != null && !expirationOverride.isBlank()) {
            try {
                long parsed = Long.parseLong(expirationOverride.trim());
                if (parsed > 0) {
                    expirationMs = parsed;
                }
            } catch (NumberFormatException ignored) {
                // keep default
            }
        }
        EXPIRATION_TIME = expirationMs;

        // Resolve and validate secret key
        String keyStr = System.getProperty("nioflow.jwtSecret");
        if (keyStr == null || keyStr.isBlank()) {
            keyStr = System.getenv("JWT_SECRET");
        }
        if (keyStr == null || keyStr.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable is missing or too short (min " + MIN_SECRET_LENGTH
                            + " chars required for security).");
        }
        if (shannonEntropy(keyStr) < MIN_ENTROPY_BITS) {
            throw new IllegalStateException(
                    "JWT_SECRET has insufficient entropy. Use a randomly generated secret with high character diversity.");
        }
        SECRET_KEY = Keys.hmacShaKeyFor(keyStr.getBytes(StandardCharsets.UTF_8));
    }

    private static void requireKey() {
        if (SECRET_KEY == null) {
            throw new IllegalStateException(
                    "JWT is not configured. Set JWT_SECRET environment variable (min " + MIN_SECRET_LENGTH
                            + " chars).");
        }
    }

    /**
     * Generates a short-lived access token with issuer pinning and unique JTI.
     *
     * @param username the subject
     * @param role     the role claim
     * @return signed JWT string
     */
    public static String generateToken(String username, String role) {
        requireKey();
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString()) // jti — unique token ID for revocation
                .issuer(ISSUER) // iss — pinned issuer
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Validates signature, expiration, and issuer.
     */
    public static boolean validateToken(String token) {
        requireKey();
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .requireIssuer(ISSUER) // reject tokens from foreign issuers
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public static String getRoleFromToken(String token) {
        return (String) getClaims(token).get("role");
    }

    /**
     * Extracts the JTI (JWT ID) from a token for revocation/blocklist lookup.
     */
    public static String getJtiFromToken(String token) {
        return getClaims(token).getId();
    }

    private static Claims getClaims(String token) {
        requireKey();
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Shannon entropy estimator — measures character diversity.
     * A 32-char secret with all identical characters has ~0 bits of entropy.
     * A good random secret has ~4-6+ bits.
     */
    static double shannonEntropy(String s) {
        if (s == null || s.isEmpty()) {
            return 0.0;
        }
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        double entropy = 0.0;
        double len = s.length();
        for (int count : freq.values()) {
            double p = count / len;
            if (p > 0) {
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        return entropy;
    }
}
