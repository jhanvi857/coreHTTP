package io.github.jhanvi857.nioflow.middleware;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChaosMiddlewareTest {

    private HttpContext ctx;
    private RouteHandler next;
    private ChaosMiddleware middleware;

    @BeforeEach
    void setUp() {
        ctx = mock(HttpContext.class);
        next = mock(RouteHandler.class);
        middleware = new ChaosMiddleware();
        when(ctx.status(any())).thenReturn(ctx);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("NIOFLOW_CHAOS_ENABLED");
    }

    @Test
    void process_chaosDisabled_isNoOp() throws Exception {
        System.setProperty("NIOFLOW_CHAOS_ENABLED", "false");
        middleware.error(500, 1.0);

        middleware.process(ctx, next);

        verify(next).handle(ctx);
        verify(ctx, never()).status(any());
    }

    @Test
    void process_chaosEnabledLatency_injectsDelay() throws Exception {
        System.setProperty("NIOFLOW_CHAOS_ENABLED", "true");
        middleware.latency(50, 1.0);

        long start = System.currentTimeMillis();
        middleware.process(ctx, next);
        long end = System.currentTimeMillis();

        assertTrue(end - start >= 50, "Should have delayed for at least 50ms");
        verify(next).handle(ctx);
    }

    @Test
    void process_chaosEnabledError_returnsError() throws Exception {
        System.setProperty("NIOFLOW_CHAOS_ENABLED", "true");
        middleware.error(503, 1.0);

        middleware.process(ctx, next);

        verify(ctx).status(HttpStatus.SERVICE_UNAVAILABLE);
        verify(next, never()).handle(ctx);
    }

    @Test
    void process_chaosEnabledDrop_dropsResponse() throws Exception {
        System.setProperty("NIOFLOW_CHAOS_ENABLED", "true");
        middleware.drop(1.0);

        middleware.process(ctx, next);

        verify(ctx).dropResponse();
        verify(next, never()).handle(ctx);
    }

    @Test
    void process_zeroProbability_neverFires() throws Exception {
        System.setProperty("NIOFLOW_CHAOS_ENABLED", "true");
        middleware.error(500, 0.0);

        middleware.process(ctx, next);

        verify(next).handle(ctx);
        verify(ctx, never()).status(any());
    }

    @Test
    void configMethods_clampProbability() {
        middleware.error(500, 1.5).latency(10, -0.5).drop(2.0);
        // Internal clamp logic should keep it between 0 and 1
    }
}
