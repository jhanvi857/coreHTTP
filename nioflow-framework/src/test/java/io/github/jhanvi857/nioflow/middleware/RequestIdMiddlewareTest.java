package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RequestIdMiddlewareTest {
    private RequestIdMiddleware middleware;
    private HttpContext ctx;
    private HttpResponse res;
    private RouteHandler next;
    private AtomicBoolean nextCalled;

    @BeforeEach
    void setUp() {
        middleware = new RequestIdMiddleware();
        ctx = mock(HttpContext.class);
        res = mock(HttpResponse.class);
        nextCalled = new AtomicBoolean(false);
        next = (c) -> {
            nextCalled.set(true);
            // Verify MDC is set during next.handle
            assertNotNull(MDC.get("requestId"));
        };

        when(ctx.getResponse()).thenReturn(res);
        MDC.clear();
    }

    @Test
    void handle_noExistingId_generatesUUID() throws Exception {
        when(ctx.header("X-Request-ID")).thenReturn(null);
        
        middleware.process(ctx, next);
        
        assertTrue(nextCalled.get());
        verify(res).addHeader(eq("X-Request-ID"), argThat(id -> id.matches("[0-9a-f-]{36}")));
        assertNull(MDC.get("requestId")); // Should be cleared after process
    }

    @Test
    void handle_existingId_preservedNotOverwritten() throws Exception {
        String existingId = "existing-123";
        when(ctx.header("X-Request-ID")).thenReturn(existingId);
        
        middleware.process(ctx, next);
        
        assertTrue(nextCalled.get());
        verify(res).addHeader(eq("X-Request-ID"), eq(existingId));
    }

    @Test
    void handle_blankId_generatesUUID() throws Exception {
        when(ctx.header("X-Request-ID")).thenReturn("  ");
        
        middleware.process(ctx, next);
        
        assertTrue(nextCalled.get());
        verify(res).addHeader(eq("X-Request-ID"), argThat(id -> id.matches("[0-9a-f-]{36}")));
    }

    @Test
    void handle_alwaysCallsNext() throws Exception {
        middleware.process(ctx, next);
        assertTrue(nextCalled.get());
    }
}
