package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.Env;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Redis-backed distributed rate limiter implementing a windowed counter.
 * Falls back to in-memory RateLimitMiddleware if Redis is unavailable.
 *
 * <p>
 * IP extraction uses the same hardened strategy as {@link RateLimitMiddleware}:
 * socket peer address by default, rightmost-non-trusted XFF entry when
 * trusted proxies are configured.
 * </p>
 */
public class RedisRateLimitMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(RedisRateLimitMiddleware.class);
    private final int maxRequests;
    private final int windowSeconds;
    private JedisPool jedisPool;
    private final RateLimitMiddleware fallback;

    /** Trusted proxy IPs — shared with the in-memory fallback. */
    private final Set<String> trustedProxies;

    public RedisRateLimitMiddleware(int maxRequests, int windowSeconds) {
        this(maxRequests, windowSeconds, Set.of());
    }

    public RedisRateLimitMiddleware(int maxRequests, int windowSeconds, Set<String> trustedProxies) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.trustedProxies = trustedProxies != null ? trustedProxies : Set.of();
        this.fallback = new RateLimitMiddleware(maxRequests, windowSeconds * 1000L, this.trustedProxies);

        String redisUrl = Env.get("NIOFLOW_REDIS_URL");
        if (redisUrl != null && !redisUrl.isBlank()) {
            try {
                this.jedisPool = new JedisPool(new JedisPoolConfig(), redisUrl);
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.ping();
                }
                logger.info("RedisRateLimitMiddleware connected to Redis at {}", redisUrl);
            } catch (Exception e) {
                logger.error("Failed to connect to Redis at {}: {}. Will fail-open to in-memory.", redisUrl,
                        e.getMessage());
                this.jedisPool = null;
            }
        } else {
            this.jedisPool = null;
        }
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        if (jedisPool == null) {
            fallback.process(ctx, next);
            return;
        }

        String clientIp = resolveClientIp(ctx);

        long now = System.currentTimeMillis() / 1000;
        long windowStart = now - (now % windowSeconds);
        String key = String.format("ratelimit:%s:%d", clientIp, windowStart);

        try (Jedis jedis = jedisPool.getResource()) {
            long count = jedis.incr(key);
            if (count == 1) {
                jedis.expire(key, windowSeconds * 2);
            }

            if (count > maxRequests) {
                ctx.status(HttpStatus.TOO_MANY_REQUESTS)
                        .json(Map.of("error", "Rate limit exceeded (distributed). Try again later."));
                return;
            }
        } catch (Exception e) {
            logger.error("Redis rate limit error, falling back to in-memory: {}", e.getMessage());
            fallback.process(ctx, next);
            return;
        }

        next.handle(ctx);
    }

    /**
     * Resolves the real client IP — mirrors
     * {@link RateLimitMiddleware#resolveClientIp}.
     */
    private String resolveClientIp(HttpContext ctx) {
        String xff = ctx.header("X-Forwarded-For");

        if (xff != null && !xff.isBlank() && !trustedProxies.isEmpty()) {
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

    private static String stripPort(String address) {
        if (address.startsWith("[")) {
            int bracketEnd = address.indexOf(']');
            if (bracketEnd > 0) {
                return address.substring(1, bracketEnd);
            }
        }
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
}
