package io.github.jhanvi857.nioflow.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);
    private static final long EXPIRATION_TIME = 86400000; // 24 hr.
    private static final SecretKey SECRET_KEY;

    static {
        String keyStr = System.getProperty("nioflow.jwtSecret");
        if (keyStr == null || keyStr.isBlank()) {
            keyStr = System.getenv("JWT_SECRET");
        }
        if (keyStr == null || keyStr.length() < 32) {
            logger.warn("JWT_SECRET not configured or too short. JwtProvider will not be functional. "
                    + "Set JWT_SECRET env var (min 32 chars) to enable JWT authentication.");
            SECRET_KEY = null;
        } else {
            SECRET_KEY = Keys.hmacShaKeyFor(keyStr.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void requireKey() {
        if (SECRET_KEY == null) {
            throw new IllegalStateException(
                    "JWT is not configured. Set JWT_SECRET environment variable (min 32 chars).");
        }
    }

    public static String generateToken(String username, String role) {
        requireKey();
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static boolean validateToken(String token) {
        requireKey();
        try {
            Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token);
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

    private static Claims getClaims(String token) {
        requireKey();
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
