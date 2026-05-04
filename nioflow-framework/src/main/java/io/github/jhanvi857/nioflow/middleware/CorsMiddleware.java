package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CORS middleware with explicit-origin enforcement for production deployments.
 */
public class CorsMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(CorsMiddleware.class);
    private final String allowedOrigin;
    private final boolean allowCredentials;

    public CorsMiddleware(String allowedOrigin) {
        this(allowedOrigin, false);
    }

    public CorsMiddleware(String allowedOrigin, boolean allowCredentials) {
        this.allowedOrigin = allowedOrigin == null ? "*" : allowedOrigin;
        this.allowCredentials = allowCredentials;

        if (this.allowCredentials && "*".equals(this.allowedOrigin)) {
            throw new IllegalArgumentException("Wildcard origin (*) cannot be used when allowCredentials is true");
        }

        if (isLocalhostOrigin(this.allowedOrigin)) {
            logger.warn("CORS allowedOrigin is set to a localhost URL ({}).", this.allowedOrigin);
        }
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        String requestOrigin = ctx.header("Origin");

        if (ctx.method().equalsIgnoreCase("OPTIONS")) {
            addCorsHeaders(ctx, requestOrigin);
            ctx.status(HttpStatus.NO_CONTENT).send("");
            return;
        }

        next.handle(ctx);
        addCorsHeaders(ctx, requestOrigin);
    }

    private void addCorsHeaders(HttpContext ctx, String requestOrigin) {
        if ("*".equals(allowedOrigin)) {
            ctx.header("Access-Control-Allow-Origin", "*");
        } else if (allowedOrigin.equals(requestOrigin)) {
            ctx.header("Access-Control-Allow-Origin", allowedOrigin);
            ctx.header("Vary", "Origin");
        } else {
            return;
        }

        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-User");
        ctx.header("Access-Control-Max-Age", "3600");

        if (allowCredentials) {
            ctx.header("Access-Control-Allow-Credentials", "true");
        }
    }

    private static boolean isLocalhostOrigin(String origin) {
        if (origin == null)
            return false;
        String lower = origin.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("localhost") || lower.contains("127.0.0.1") || lower.contains("0.0.0.0");
    }
}
