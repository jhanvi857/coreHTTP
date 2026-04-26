package io.github.jhanvi857.nioflow.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jhanvi857.nioflow.Env;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LoggerMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(LoggerMiddleware.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final boolean JSON_LOGGING = "json".equalsIgnoreCase(Env.get("NIOFLOW_LOG_FORMAT"));
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        long startTime = System.currentTimeMillis();
        
        String requestId = MDC.get("requestId");
        String clientIp = ctx.header("X-Forwarded-For") != null ? ctx.header("X-Forwarded-For") : ctx.remoteAddress();

        // Populate MDC for standard logging or other middleware
        MDC.put("method", ctx.method());
        MDC.put("path", ctx.path());
        MDC.put("remoteAddr", clientIp != null ? clientIp : "unknown");

        try {
            next.handle(ctx);
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = ctx.getResponse().getStatus().getCode();
            
            MDC.put("status", String.valueOf(statusCode));
            MDC.put("duration", String.valueOf(duration));

            if (JSON_LOGGING) {
                logJson("INFO", ctx, statusCode, duration, clientIp, requestId, null);
            } else {
                logger.info("Request processed successfully: {} {} -> {}",
                        ctx.method(),
                        ctx.path(),
                        statusCode);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status", "500");
            MDC.put("duration", String.valueOf(duration));
            MDC.put("error", e.getMessage());

            if (JSON_LOGGING) {
                logJson("ERROR", ctx, 500, duration, clientIp, requestId, e.getMessage());
            } else {
                logger.error("Request failed: {} {} - {}",
                        ctx.method(),
                        ctx.path(),
                        e.getMessage());
            }
            throw e;
        } finally {
            MDC.clear();
        }
    }

    private void logJson(String level, HttpContext ctx, int statusCode, long duration, String clientIp, String requestId, String error) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("timestamp", ISO_FORMATTER.format(Instant.now()));
            root.put("level", level);
            root.put("method", ctx.method());
            root.put("path", ctx.path());
            root.put("statusCode", statusCode);
            root.put("latencyMs", duration);
            root.put("clientIp", clientIp != null ? clientIp : "unknown");
            root.put("requestId", requestId != null ? requestId : "unknown");
            if (error != null) {
                root.put("error", error);
            }
            logger.info(root.toString());
        } catch (Exception e) {
            logger.error("Failed to log JSON: {}", e.getMessage());
        }
    }
}

