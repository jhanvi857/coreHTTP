package io.github.jhanvi857.nioflow.middleware;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CorsMiddlewareTest {

    private HttpContext ctx;
    private RouteHandler next;

    @BeforeEach
    void setUp() {
        ctx = mock(HttpContext.class);
        next = mock(RouteHandler.class);
        when(ctx.status(any())).thenReturn(ctx);
    }

    @Test
    void process_matchingOrigin_setsHeader() throws Exception {
        CorsMiddleware middleware = new CorsMiddleware("https://example.com");
        when(ctx.method()).thenReturn("GET");
        when(ctx.header("Origin")).thenReturn("https://example.com");

        middleware.process(ctx, next);

        verify(ctx).header("Access-Control-Allow-Origin", "https://example.com");
        verify(next).handle(ctx);
    }

    @Test
    void process_mismatchingOrigin_headerAbsent() throws Exception {
        CorsMiddleware middleware = new CorsMiddleware("https://example.com");
        when(ctx.method()).thenReturn("GET");
        when(ctx.header("Origin")).thenReturn("https://evil.com");

        middleware.process(ctx, next);

        verify(ctx, never()).header(eq("Access-Control-Allow-Origin"), anyString());
        verify(next).handle(ctx);
    }

    @Test
    void process_wildcardOrigin_setsHeader() throws Exception {
        CorsMiddleware middleware = new CorsMiddleware("*");
        when(ctx.method()).thenReturn("GET");
        when(ctx.header("Origin")).thenReturn("https://any.com");

        middleware.process(ctx, next);

        verify(ctx).header("Access-Control-Allow-Origin", "*");
        verify(next).handle(ctx);
    }

    @Test
    void process_preflightOptions_returns204AndHeaders() throws Exception {
        CorsMiddleware middleware = new CorsMiddleware("https://example.com");
        when(ctx.method()).thenReturn("OPTIONS");
        when(ctx.header("Origin")).thenReturn("https://example.com");

        middleware.process(ctx, next);

        verify(ctx).status(HttpStatus.NO_CONTENT);
        verify(ctx).header("Access-Control-Allow-Origin", "https://example.com");
        verify(next, never()).handle(ctx);
    }

    @Test
    void constructor_localhostOrigin_logsWarn() {
        new CorsMiddleware("http://localhost:8080");
    }

    @Test
    void constructor_wildcardWithCredentials_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new CorsMiddleware("*", true));
    }

    @Test
    void process_withCredentials_setsHeader() throws Exception {
        CorsMiddleware middleware = new CorsMiddleware("https://example.com", true);
        when(ctx.method()).thenReturn("GET");
        when(ctx.header("Origin")).thenReturn("https://example.com");

        middleware.process(ctx, next);

        verify(ctx).header("Access-Control-Allow-Credentials", "true");
    }
}
