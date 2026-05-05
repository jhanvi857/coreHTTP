package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.observability.TracerConfig;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

/**
 * Middleware for OpenTelemetry tracing.
 */
public class TracingMiddleware implements Middleware {
    private static final Tracer DEFAULT_TRACER = TracerConfig.get().getTracer("nioflow-http");
    
    private final Tracer tracer;

    private static final AttributeKey<String> HTTP_METHOD = AttributeKey.stringKey("http.method");
    private static final AttributeKey<String> HTTP_ROUTE = AttributeKey.stringKey("http.route");
    private static final AttributeKey<String> HTTP_CLIENT_IP = AttributeKey.stringKey("http.client_ip");
    private static final AttributeKey<Long> HTTP_STATUS_CODE = AttributeKey.longKey("http.status_code");

    public TracingMiddleware() {
        this(DEFAULT_TRACER);
    }

    public TracingMiddleware(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        String spanName = String.format("%s %s", ctx.method(), ctx.routePattern());
        
        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            String traceId = span.getSpanContext().getTraceId();
            ctx.header("X-Trace-Id", traceId);
            
            span.setAttribute(HTTP_METHOD, ctx.method());
            span.setAttribute(HTTP_ROUTE, ctx.routePattern());
            String clientIp = ctx.header("X-Forwarded-For");
            if (clientIp == null) {
                clientIp = ctx.remoteAddress();
            }
            span.setAttribute(HTTP_CLIENT_IP, clientIp != null ? clientIp : "unknown");
            
            next.handle(ctx);
            
            int statusCode = ctx.getResponse().getStatus().getCode();
            span.setAttribute(HTTP_STATUS_CODE, (long) statusCode);
            if (statusCode >= 500) {
                span.setStatus(StatusCode.ERROR);
            }
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.setAttribute(HTTP_STATUS_CODE, 500L);
            throw e;
        } finally {
            span.end();
        }
    }
}
