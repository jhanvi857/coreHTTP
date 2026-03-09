package com.jhanvi857.coreHTTP.middleware;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;
import com.jhanvi857.coreHTTP.routing.RouteHandler;
import java.io.IOException;
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
    public HttpResponse process(HttpRequest request, RouteHandler next) throws IOException {
        String clientIp = request.getHeaders().getOrDefault("X-Forwarded-For", "global");
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
                return next.handle(request);
            }

            if (data.count.incrementAndGet() > maxRequests) {
                return new HttpResponse(HttpStatus.TOO_MANY_REQUESTS,
                        "{\"error\": \"Rate limit exceeded. Try again in a few seconds.\"}");
            }
        }

        return next.handle(request);
    }

    private static class RateData {
        final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger count = new AtomicInteger(0);
    }
}
