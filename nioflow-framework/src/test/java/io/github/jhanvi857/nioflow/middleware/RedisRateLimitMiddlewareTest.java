package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RedisRateLimitMiddlewareTest {
    private JedisPool mockPool;
    private Jedis mockJedis;
    private HttpContext ctx;
    private HttpResponse res;
    private RouteHandler next;
    private AtomicBoolean nextCalled;

    @BeforeEach
    void setUp() {
        mockPool = mock(JedisPool.class);
        mockJedis = mock(Jedis.class);
        ctx = mock(HttpContext.class);
        res = mock(HttpResponse.class);
        nextCalled = new AtomicBoolean(false);
        next = (c) -> nextCalled.set(true);

        when(mockPool.getResource()).thenReturn(mockJedis);
        when(ctx.getResponse()).thenReturn(res);
        when(ctx.remoteAddress()).thenReturn("127.0.0.1:1234");
        
        // Mock fluent methods that return HttpContext
        when(ctx.status(any())).thenReturn(ctx);
        when(ctx.status(anyInt())).thenReturn(ctx);
        when(ctx.header(anyString(), anyString())).thenReturn(ctx);
        
        // json() and send() are void, so we don't use when() on them.
    }

    @Test
    void handle_withinLimit_callsNext() throws Exception {
        RedisRateLimitMiddleware middleware = new RedisRateLimitMiddleware(10, 60, Set.of(), mockPool);
        when(mockJedis.incr(anyString())).thenReturn(5L);

        middleware.process(ctx, next);

        assertTrue(nextCalled.get());
    }

    @Test
    void handle_exceedsLimit_returns429() throws Exception {
        RedisRateLimitMiddleware middleware = new RedisRateLimitMiddleware(10, 60, Set.of(), mockPool);
        when(mockJedis.incr(anyString())).thenReturn(11L);

        middleware.process(ctx, next);

        assertFalse(nextCalled.get());
        verify(ctx).status(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void handle_firstRequest_setsExpire() throws Exception {
        RedisRateLimitMiddleware middleware = new RedisRateLimitMiddleware(10, 60, Set.of(), mockPool);
        when(mockJedis.incr(anyString())).thenReturn(1L);

        middleware.process(ctx, next);

        verify(mockJedis).expire(anyString(), eq(120L));
    }

    @Test
    void handle_redisError_fallsBackToMemory() throws Exception {
        when(mockJedis.incr(anyString())).thenThrow(new RuntimeException("Redis Down"));
        
        RedisRateLimitMiddleware middleware = new RedisRateLimitMiddleware(10, 60, Set.of(), mockPool);
        
        // Should fall back to RateLimitMiddleware and still call next if within limits there
        middleware.process(ctx, next);
        
        assertTrue(nextCalled.get());
    }

    @Test
    void handle_noPool_fallsBackToMemory() throws Exception {
        RedisRateLimitMiddleware middleware = new RedisRateLimitMiddleware(10, 60, Set.of(), null);
        
        middleware.process(ctx, next);
        
        assertTrue(nextCalled.get());
    }

    @Test
    void resolveClientIp_trustedProxies() throws Exception {
        RedisRateLimitMiddleware middleware = new RedisRateLimitMiddleware(10, 60, Set.of("10.0.0.1"), mockPool);
        when(ctx.header("X-Forwarded-For")).thenReturn("1.2.3.4, 10.0.0.1");
        when(mockJedis.incr(anyString())).thenReturn(1L);

        middleware.process(ctx, next);
        
        assertTrue(nextCalled.get());
    }

    @Test
    void stripPort_ipv6() throws Exception {
        RedisRateLimitMiddleware middleware = new RedisRateLimitMiddleware(10, 60, Set.of(), mockPool);
        when(ctx.remoteAddress()).thenReturn("[2001:db8::1]:1234");
        when(mockJedis.incr(anyString())).thenReturn(1L);

        middleware.process(ctx, next);
        assertTrue(nextCalled.get());
    }
}
