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

/**
 * Redis-backed distributed rate limiter implementing a windowed counter.
 * Falls back to in-memory RateLimitMiddleware if Redis is unavailable.
 */
public class RedisRateLimitMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(RedisRateLimitMiddleware.class);
    private final int maxRequests;
    private final int windowSeconds;
    private JedisPool jedisPool;
    private final RateLimitMiddleware fallback;

    public RedisRateLimitMiddleware(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.fallback = new RateLimitMiddleware(maxRequests, windowSeconds * 1000L);
        
        String redisUrl = Env.get("NIOFLOW_REDIS_URL");
        if (redisUrl != null && !redisUrl.isBlank()) {
            try {
                this.jedisPool = new JedisPool(new JedisPoolConfig(), redisUrl);
                // Test connection
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.ping();
                }
                logger.info("RedisRateLimitMiddleware connected to Redis at {}", redisUrl);
            } catch (Exception e) {
                logger.error("Failed to connect to Redis at {}: {}. Will fail-open to in-memory.", redisUrl, e.getMessage());
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

        String clientIp = ctx.header("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = ctx.remoteAddress() != null ? ctx.remoteAddress() : "unknown";
        }
        
        // Basic fixed-window counter using the requested key format
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
}
