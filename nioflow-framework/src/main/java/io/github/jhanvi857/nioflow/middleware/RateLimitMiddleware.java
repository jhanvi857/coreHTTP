package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory sliding-window rate limiter with hardened IP extraction.
 *
 * <h3>IP Resolution Strategy</h3>
 * <ol>
 * <li>If trusted proxies are configured, uses the <b>rightmost non-trusted</b>
 * entry in the {@code X-Forwarded-For} chain — this is the last hop
 * before the first trusted proxy and is resistant to client-side spoofing.</li>
 * <li>Falls back to the <b>socket peer address</b> ({@code remoteAddress})
 * when no proxy header is present or no proxies are trusted.</li>
 * </ol>
 */
public class RateLimitMiddleware implements Middleware {
    private final int maxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, RateData> clientRequests = new ConcurrentHashMap<>();
    private static final int MAX_TRACKED_CLIENTS = 10000;

    /** IP addresses of trusted reverse proxies (e.g. your load balancer). */
    private final Set<String> trustedProxies;

    public RateLimitMiddleware(int maxRequests, long windowMs) {
        this(maxRequests, windowMs, Set.of());
    }

    /**
     * @param trustedProxies Set of known proxy IPs. When non-empty, the rightmost
     *                       non-trusted entry in X-Forwarded-For is used as the
     *                       client IP.
     */
    public RateLimitMiddleware(int maxRequests, long windowMs, Set<String> trustedProxies) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.trustedProxies = trustedProxies != null ? trustedProxies : Set.of();
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        String clientIp = resolveClientIp(ctx);

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

    /**
     * Resolves the real client IP using the rightmost-non-trusted strategy.
     * This defeats trivial bypass by spoofing X-Forwarded-For.
     */
    String resolveClientIp(HttpContext ctx) {
        String xff = ctx.header("X-Forwarded-For");

        if (xff != null && !xff.isBlank() && !trustedProxies.isEmpty()) {
            // Walk the XFF chain from right to left; the rightmost entry that is
            // not in the trusted set is the true client.
            String[] ips = xff.split(",");
            for (int i = ips.length - 1; i >= 0; i--) {
                String candidate = ips[i].trim();
                if (!candidate.isEmpty() && !trustedProxies.contains(candidate)) {
                    return candidate;
                }
            }
        }

        String remote = ctx.remoteAddress();
        if (remote == null || remote.isBlank()) {
            return "unknown";
        }
        return stripPort(remote);
    }

    /**
     * Strips the port portion from an address string like "127.0.0.1:54321".
     * Handles IPv6 bracket notation like "[::1]:54321".
     */
    private static String stripPort(String address) {
        // IPv6 bracket notation
        if (address.startsWith("[")) {
            int bracketEnd = address.indexOf(']');
            if (bracketEnd > 0) {
                return address.substring(1, bracketEnd);
            }
        }
        // IPv4 or hostname:port
        int lastColon = address.lastIndexOf(':');
        if (lastColon > 0) {
            String afterColon = address.substring(lastColon + 1);
            try {
                Integer.parseInt(afterColon);
                return address.substring(0, lastColon);
            } catch (NumberFormatException e) {
            }
        }
        return address;
    }

    private static class RateData {
        final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger count = new AtomicInteger(0);
    }
}
