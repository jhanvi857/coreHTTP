package com.jhanvi857.nioflow.middleware;

import com.jhanvi857.nioflow.routing.HttpContext;
import com.jhanvi857.nioflow.routing.RouteHandler;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsMiddleware implements Middleware {
    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong totalErrors = new AtomicLong(0);
    private static final ConcurrentHashMap<String, Long> latencies = new ConcurrentHashMap<>();

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        totalRequests.incrementAndGet();
        long start = System.nanoTime();

        try {
            next.handle(ctx);
            if (ctx.getResponse().getStatus().getCode() >= 400) {
                totalErrors.incrementAndGet();
            }

            long end = System.nanoTime();
            recordLatency(ctx.path(), (end - start) / 1000000);

        } catch (Exception e) {
            totalErrors.incrementAndGet();
            throw e;
        }
    }

    private static final int MAX_TRACKED_PATHS = 500;

    private void recordLatency(String path, long ms) {
        // Normalizing paths to prevent unbounded map growth from dynamic path segments.
        // e.g., /api/tasks/123 → /api/tasks/{id}
        String normalized = normalizePath(path);
        if (latencies.size() < MAX_TRACKED_PATHS || latencies.containsKey(normalized)) {
            latencies.merge(normalized, ms, (oldVal, newVal) -> (oldVal + newVal) / 2);
        }
    }

    private static String normalizePath(String path) {
        // Replacing trailing numeric path segments with {id}
        return path.replaceAll("/\\d+$", "/{id}");
    }

    public static String getMetricsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP nioflow_requests_total Total number of HTTP requests\n");
        sb.append("# TYPE nioflow_requests_total counter\n");
        sb.append("nioflow_requests_total ").append(totalRequests.get()).append("\n\n");

        sb.append("# HELP nioflow_errors_total Total number of HTTP errors\n");
        sb.append("# TYPE nioflow_errors_total counter\n");
        sb.append("nioflow_errors_total ").append(totalErrors.get()).append("\n\n");

        sb.append("# HELP nioflow_request_latency_avg_ms Average request latency in ms per path\n");
        sb.append("# TYPE nioflow_request_latency_avg_ms gauge\n");
        latencies.forEach((path, lat) -> {
            sb.append("nioflow_request_latency_avg_ms{path=\"").append(path).append("\"} ").append(lat).append("\n");
        });

        return sb.toString();
    }
}
