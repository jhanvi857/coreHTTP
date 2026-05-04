package io.github.jhanvi857.nioflow.middleware;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class RateLimitMiddlewareTest {

    private HttpContext ctx;
    private RouteHandler next;

    @BeforeEach
    void setUp() {
        ctx = mock(HttpContext.class);
        next = mock(RouteHandler.class);
        when(ctx.status(any())).thenReturn(ctx);
    }

    @Test
    void process_withinLimit_passesThrough() throws Exception {
        RateLimitMiddleware middleware = new RateLimitMiddleware(2, 1000);
        when(ctx.remoteAddress()).thenReturn("127.0.0.1:12345");

        // 1st request
        middleware.process(ctx, next);
        verify(next, times(1)).handle(ctx);

        // 2nd request
        middleware.process(ctx, next);
        verify(next, times(2)).handle(ctx);
    }

    @Test
    void process_limitExceeded_returns429() throws Exception {
        RateLimitMiddleware middleware = new RateLimitMiddleware(1, 1000);
        when(ctx.remoteAddress()).thenReturn("127.0.0.1:12345");

        // 1st request
        middleware.process(ctx, next);
        verify(next, times(1)).handle(ctx);

        // 2nd request
        middleware.process(ctx, next);
        verify(ctx).status(HttpStatus.TOO_MANY_REQUESTS);
        verify(next, times(1)).handle(ctx); // Should not be called again
    }

    @Test
    void process_windowExpires_resetsCounter() throws Exception {
        RateLimitMiddleware middleware = new RateLimitMiddleware(1, 100);
        when(ctx.remoteAddress()).thenReturn("127.0.0.1:12345");

        // 1st request
        middleware.process(ctx, next);

        // Wait for window to expire
        Thread.sleep(150);

        // 2nd request should pass
        middleware.process(ctx, next);
        verify(next, times(2)).handle(ctx);
    }

    @Test
    void resolveClientIp_noProxy_usesRemoteAddressWithoutPort() {
        RateLimitMiddleware middleware = new RateLimitMiddleware(1, 1000);
        when(ctx.remoteAddress()).thenReturn("192.168.1.1:54321");

        String ip = middleware.resolveClientIp(ctx);
        assertEquals("192.168.1.1", ip);
    }

    @Test
    void resolveClientIp_ipv6_stripsPort() {
        RateLimitMiddleware middleware = new RateLimitMiddleware(1, 1000);
        when(ctx.remoteAddress()).thenReturn("[2001:db8::1]:54321");

        String ip = middleware.resolveClientIp(ctx);
        assertEquals("2001:db8::1", ip);
    }

    @Test
    void resolveClientIp_withTrustedProxy_usesRightmostNonTrusted() {
        Set<String> trusted = Set.of("10.0.0.1", "10.0.0.2");
        RateLimitMiddleware middleware = new RateLimitMiddleware(1, 1000, trusted);

        // Chain: Client (1.2.3.4) -> Spoofed (5.6.7.8) -> LB1 (10.0.0.2) -> LB2
        // (10.0.0.1)
        when(ctx.header("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8, 10.0.0.2");
        when(ctx.remoteAddress()).thenReturn("10.0.0.1:80");

        String ip = middleware.resolveClientIp(ctx);
        assertEquals("5.6.7.8", ip);
    }

    @Test
    void resolveClientIp_allTrusted_fallsBackToRemote() {
        Set<String> trusted = Set.of("10.0.0.1", "10.0.0.2");
        RateLimitMiddleware middleware = new RateLimitMiddleware(1, 1000, trusted);

        when(ctx.header("X-Forwarded-For")).thenReturn("10.0.0.2, 10.0.0.1");
        when(ctx.remoteAddress()).thenReturn("127.0.0.1:12345");

        String ip = middleware.resolveClientIp(ctx);
        assertEquals("127.0.0.1", ip);
    }

    @Test
    void resolveClientIp_nullRemote_returnsUnknown() {
        RateLimitMiddleware middleware = new RateLimitMiddleware(1, 1000);
        when(ctx.remoteAddress()).thenReturn(null);

        String ip = middleware.resolveClientIp(ctx);
        assertEquals("unknown", ip);
    }

    @Test
    void stripPort_noPort_returnsAsIs() {
        RateLimitMiddleware middleware = new RateLimitMiddleware(1, 1000);
        when(ctx.remoteAddress()).thenReturn("my-host");
        assertEquals("my-host", middleware.resolveClientIp(ctx));
    }
}
