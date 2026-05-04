package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/**
 * Basic CSRF protection middleware using the Synchronizer Token Pattern.
 */
public class CsrfMiddleware implements Middleware {
    private static final String CSRF_COOKIE_NAME = "CSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        String method = ctx.method().toUpperCase();

        if (SAFE_METHODS.contains(method)) {
            ensureCsrfCookie(ctx);
            next.handle(ctx);
            return;
        }

        String headerToken = ctx.header(CSRF_HEADER_NAME);
        String cookieToken = extractCookie(ctx, CSRF_COOKIE_NAME);

        if (cookieToken == null || headerToken == null || !cookieToken.equals(headerToken)) {
            ctx.status(HttpStatus.FORBIDDEN)
                    .json(java.util.Map.of("error", "Invalid or missing CSRF token"));
            return;
        }

        next.handle(ctx);
    }

    private void ensureCsrfCookie(HttpContext ctx) {
        String existing = extractCookie(ctx, CSRF_COOKIE_NAME);
        if (existing == null) {
            byte[] tokenBytes = new byte[32];
            secureRandom.nextBytes(tokenBytes);
            String newToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            ctx.getResponse().addHeader("Set-Cookie", CSRF_COOKIE_NAME + "=" + newToken + "; Path=/; SameSite=Lax");
        }
    }

    private String extractCookie(HttpContext ctx, String name) {
        String cookieHeader = ctx.header("Cookie");
        if (cookieHeader == null)
            return null;

        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && parts[0].equals(name)) {
                return parts[1];
            }
        }
        return null;
    }
}
