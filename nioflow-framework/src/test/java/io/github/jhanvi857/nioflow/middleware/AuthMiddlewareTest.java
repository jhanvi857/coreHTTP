package io.github.jhanvi857.nioflow.middleware;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.jhanvi857.nioflow.auth.JwtProvider;
import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AuthMiddlewareTest {

    @BeforeAll
    static void init() {
        System.setProperty("nioflow.jwtSecret", "q9z2K8hP6mXvW3rY1nB7vC5xZ0pQ9sL2");
    }

    private HttpContext ctx;
    private HttpRequest req;
    private RouteHandler next;

    @BeforeEach
    void setUp() {
        ctx = mock(HttpContext.class);
        req = mock(HttpRequest.class);
        next = mock(RouteHandler.class);
        when(ctx.getRequest()).thenReturn(req);
        when(ctx.status(any())).thenReturn(ctx);

        System.setProperty("NIOFLOW_BIND_ADDRESS", "127.0.0.1");
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("NIOFLOW_DISABLE_AUTH");
        System.clearProperty("NIOFLOW_BIND_ADDRESS");
    }

    @Test
    void process_validToken_passesThrough() throws Exception {
        try (MockedStatic<JwtProvider> jwtMock = mockStatic(JwtProvider.class)) {
            when(ctx.header("Authorization")).thenReturn("Bearer valid-token");
            jwtMock.when(() -> JwtProvider.validateToken("valid-token")).thenReturn(true);
            jwtMock.when(() -> JwtProvider.getUsernameFromToken("valid-token")).thenReturn("user1");
            jwtMock.when(() -> JwtProvider.getRoleFromToken("valid-token")).thenReturn("admin");

            AuthMiddleware middleware = new AuthMiddleware();
            middleware.process(ctx, next);

            verify(req).addHeader("X-Auth-User", "user1");
            verify(req).addHeader("X-Auth-Role", "admin");
            verify(next).handle(ctx);
        }
    }

    @Test
    void process_noHeader_returns401() throws Exception {
        when(ctx.header("Authorization")).thenReturn(null);

        AuthMiddleware middleware = new AuthMiddleware();
        middleware.process(ctx, next);

        verify(ctx).status(HttpStatus.UNAUTHORIZED);
        verify(next, never()).handle(ctx);
    }

    @Test
    void process_malformedHeader_returns401() throws Exception {
        when(ctx.header("Authorization")).thenReturn("Basic abc");

        AuthMiddleware middleware = new AuthMiddleware();
        middleware.process(ctx, next);

        verify(ctx).status(HttpStatus.UNAUTHORIZED);
        verify(next, never()).handle(ctx);
    }

    @Test
    void process_invalidToken_returns401() throws Exception {
        try (MockedStatic<JwtProvider> jwtMock = mockStatic(JwtProvider.class)) {
            when(ctx.header("Authorization")).thenReturn("Bearer invalid-token");
            jwtMock.when(() -> JwtProvider.validateToken("invalid-token")).thenReturn(false);

            AuthMiddleware middleware = new AuthMiddleware();
            middleware.process(ctx, next);

            verify(ctx).status(HttpStatus.UNAUTHORIZED);
            verify(next, never()).handle(ctx);
        }
    }

    @Test
    void process_disableAuthOnLoopback_skipsAuth() throws Exception {
        System.setProperty("NIOFLOW_DISABLE_AUTH", "true");
        System.setProperty("NIOFLOW_BIND_ADDRESS", "127.0.0.1");

        AuthMiddleware middleware = new AuthMiddleware();
        middleware.process(ctx, next);

        verify(next).handle(ctx);
        verify(ctx, never()).header(anyString());
    }

    @Test
    void constructor_disableAuthOnPublicIp_throwsException() {
        System.setProperty("NIOFLOW_DISABLE_AUTH", "true");
        System.setProperty("NIOFLOW_BIND_ADDRESS", "192.168.1.1");

        assertThrows(IllegalStateException.class, AuthMiddleware::new);
    }
}
