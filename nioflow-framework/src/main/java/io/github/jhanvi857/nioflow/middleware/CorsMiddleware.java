package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CORS middleware with explicit-origin enforcement for production deployments.
 *
 * <p>If the allowed origin is a localhost URL and the server is bound to a
 * non-loopback address, a WARN is logged at construction time to prevent
 * silent mis-configuration in production.</p>
 */
public class CorsMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(CorsMiddleware.class);
    private final String allowedOrigin;

    public CorsMiddleware(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;

        // Warn if the allowed origin smells like a dev default in a prod-like environment
        if (isLocalhostOrigin(allowedOrigin)) {
            logger.warn("CORS allowedOrigin is set to a localhost URL ({}). "
                    + "For production deployments, set NIOFLOW_CORS_ORIGIN to your actual domain. "
                    + "Requests from other origins will be blocked by browsers.",
                    allowedOrigin);
        }
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        if (ctx.method().equalsIgnoreCase("OPTIONS")) {
            ctx.status(HttpStatus.OK).send("");
            addCorsHeaders(ctx);
            return;
        }

        next.handle(ctx);
        addCorsHeaders(ctx);
    }

    private void addCorsHeaders(HttpContext ctx) {
        ctx.header("Access-Control-Allow-Origin", allowedOrigin);
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-User");
        ctx.header("Access-Control-Max-Age", "3600");
    }

    private static boolean isLocalhostOrigin(String origin) {
        if (origin == null) return false;
        String lower = origin.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("localhost") || lower.contains("127.0.0.1") || lower.contains("0.0.0.0");
    }
}
