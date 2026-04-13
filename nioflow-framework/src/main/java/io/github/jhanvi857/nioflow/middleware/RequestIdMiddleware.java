package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.slf4j.MDC;
import java.util.UUID;

/**
 * Middleware to ensure every request has a unique ID for correlation across logs.
 */
public class RequestIdMiddleware implements Middleware {
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_KEY = "requestId";

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        String requestId = ctx.header(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Tag the response for the client
        ctx.getResponse().addHeader(REQUEST_ID_HEADER, requestId);
        
        // Add to MDC for logging
        try {
            MDC.put(MDC_KEY, requestId);
            next.handle(ctx);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
