package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimitMiddleware implements Middleware {
    private final int maxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, RateData> clientRequests = new ConcurrentHashMap<>();
    private static final int MAX_TRACKED_CLIENTS = 10000;

    public RateLimitMiddleware(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        String clientIp = ctx.header("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = "global";
        }
        int commaIdx = clientIp.indexOf(',');
        if (commaIdx > 0) {
            clientIp = clientIp.substring(0, commaIdx);
        }
        clientIp = clientIp.trim();

        if (clientRequests.size() > MAX_TRACKED_CLIENTS) {
            clientRequests.clear();
        }

        RateData data = clientRequests.computeIfAbsent(clientIp, k -> new RateData());
        long now = System.currentTimeMillis();

        synchronized (data) {
            if (now - data.windowStart.get() > windowMs) {
                data.windowStart.set(now);
                data.count.set(1);
            } else if (data.count.incrementAndGet() > maxRequests) {
                ctx.status(HttpStatus.TOO_MANY_REQUESTS)
                        .json(java.util.Map.of("error", "Rate limit exceeded. Try again in a few seconds."));
                return;
            }
        }

        next.handle(ctx);
    }

    private static class RateData {
        final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger count = new AtomicInteger(0);
    }
}
