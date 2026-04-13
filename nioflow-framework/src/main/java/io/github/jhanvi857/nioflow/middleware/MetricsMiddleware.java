package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import io.github.jhanvi857.nioflow.observability.MetricsRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;

public class MetricsMiddleware implements Middleware {
    private static final Counter totalRequests = Counter.builder("nioflow_requests_total")
            .description("Total number of HTTP requests")
            .register(MetricsRegistry.getRegistry());

    private static final Counter totalErrors = Counter.builder("nioflow_errors_total")
            .description("Total number of HTTP errors")
            .register(MetricsRegistry.getRegistry());

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        totalRequests.increment();
        long start = System.nanoTime();

        try {
            next.handle(ctx);
            if (ctx.getResponse().getStatus().getCode() >= 400) {
                totalErrors.increment();
            }

            long end = System.nanoTime();
            Timer.builder("nioflow_request_latency")
                 .description("Request latency in milliseconds")
                 .tag("path", normalizePath(ctx.path()))
                 .tag("method", ctx.method())
                 .tag("status", String.valueOf(ctx.getResponse().getStatus().getCode()))
                 .register(MetricsRegistry.getRegistry())
                 .record(end - start, TimeUnit.NANOSECONDS);

        } catch (Exception e) {
            totalErrors.increment();
            throw e;
        }
    }

    private static String normalizePath(String path) {
        // Simple regex to replace IDs in path for metric tags
        return path.replaceAll("/[0-9a-fA-F-]{36}(?=/|$)", "/{id}") // UUIDs
                  .replaceAll("/\\d+(?=/|$)", "/{id}");            // Integers
    }

    public static String getMetricsReport() {
        return MetricsRegistry.scrape();
    }
}

