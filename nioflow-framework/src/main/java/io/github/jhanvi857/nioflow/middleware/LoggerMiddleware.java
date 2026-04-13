package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LoggerMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(LoggerMiddleware.class);

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        long startTime = System.currentTimeMillis();
        
        // Populate MDC with common request fields for structured logging
        MDC.put("method", ctx.method());
        MDC.put("path", ctx.path());
        MDC.put("remoteAddr", ctx.header("X-Forwarded-For") != null ? ctx.header("X-Forwarded-For") : "unknown");

        try {
            next.handle(ctx);
            long duration = System.currentTimeMillis() - startTime;
            
            MDC.put("status", String.valueOf(ctx.getResponse().getStatus().getCode()));
            MDC.put("duration", String.valueOf(duration));

            logger.info("Request processed successfully: {} {} -> {}",
                    ctx.method(),
                    ctx.path(),
                    ctx.getResponse().getStatus().getCode());
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status", "500");
            MDC.put("duration", String.valueOf(duration));
            MDC.put("error", e.getMessage());

            logger.error("Request failed: {} {} - {}",
                    ctx.method(),
                    ctx.path(),
                    e.getMessage());
            throw e;
        } finally {
            // Clean up MDC to prevent context leakage across threads
            MDC.clear();
        }
    }
}

