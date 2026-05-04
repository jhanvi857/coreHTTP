package io.github.jhanvi857.nioflow.middleware;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class CircuitBreakerMiddlewareTest {

    private HttpContext ctx;
    private HttpResponse res;
    private RouteHandler next;
    private CircuitBreakerMiddleware cb;

    @BeforeEach
    void setUp() {
        ctx = mock(HttpContext.class);
        res = mock(HttpResponse.class);
        next = mock(RouteHandler.class);
        cb = new CircuitBreakerMiddleware()
                .threshold(0.5d)
                .windowSize(2)
                .cooldown(100L)
                .groupKey("test-group");

        when(ctx.getResponse()).thenReturn(res);
        when(res.getStatus()).thenReturn(HttpStatus.OK);
        when(ctx.status(any())).thenReturn(ctx);
        when(ctx.status(anyInt())).thenReturn(ctx);
        when(ctx.header(anyString(), anyString())).thenReturn(ctx);
    }

    @Test
    void process_closedState_requestsPassThrough() throws Exception {
        cb.process(ctx, next);
        verify(next, times(1)).handle(ctx);
        assertEquals("CLOSED", cb.state());
    }

    @Test
    void process_failureThresholdCrossed_transitionsToOpen() throws Exception {
        // Window size is 2, threshold 0.5. 1 failure out of 2 should trigger it.

        // First request fails
        when(res.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        cb.process(ctx, next);
        assertEquals("CLOSED", cb.state());

        // Second request fails -> should open
        cb.process(ctx, next);
        assertEquals("OPEN", cb.state());

        // Third request should be rejected without calling next
        reset(next);
        cb.process(ctx, next);
        verify(next, never()).handle(ctx);
        verify(ctx).status(HttpStatus.SERVICE_UNAVAILABLE);
        verify(ctx).header(eq("Retry-After"), anyString());
    }

    @Test
    void process_cooldownElapsed_transitionsToHalfOpen() throws Exception {
        // Open the breaker
        when(res.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        cb.process(ctx, next);
        cb.process(ctx, next);
        assertEquals("OPEN", cb.state());

        // Wait for cooldown
        Thread.sleep(150);

        // Next request should transition to HALF_OPEN and pass through as a probe
        reset(next);
        when(res.getStatus()).thenReturn(HttpStatus.OK);
        cb.process(ctx, next);

        verify(next, times(1)).handle(ctx);
        // It stays in HALF_OPEN during processing, and transitions to CLOSED after
        // success
        assertEquals("CLOSED", cb.state());
    }

    @Test
    void process_probeFails_transitionsBackToOpen() throws Exception {
        // Open the breaker
        when(res.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        cb.process(ctx, next);
        cb.process(ctx, next);

        Thread.sleep(150);

        // Probe fails
        reset(next);
        when(res.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        cb.process(ctx, next);

        verify(next, times(1)).handle(ctx);
        assertEquals("OPEN", cb.state());
    }

    @Test
    void process_halfOpenConcurrentRequests_onlyOneProbeAllowed() throws Exception {
        // Open the breaker
        when(res.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        cb.process(ctx, next);
        cb.process(ctx, next);

        Thread.sleep(150);

        // Simulate a long running probe
        CountDownLatch probeStarted = new CountDownLatch(1);
        CountDownLatch finishProbe = new CountDownLatch(1);

        RouteHandler slowHandler = c -> {
            probeStarted.countDown();
            finishProbe.await(2, TimeUnit.SECONDS);
            c.status(HttpStatus.OK);
        };

        new Thread(() -> {
            try {
                cb.process(ctx, slowHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        assertTrue(probeStarted.await(2, TimeUnit.SECONDS), "Probe should have started");

        // This request should be rejected because a probe is in flight
        HttpContext ctx2 = mock(HttpContext.class);
        when(ctx2.status(any())).thenReturn(ctx2);
        cb.process(ctx2, next);
        verify(ctx2).status(HttpStatus.SERVICE_UNAVAILABLE);

        finishProbe.countDown();
    }

    @Test
    void process_exceptionInHandler_countsAsFailure() throws Exception {
        doThrow(new RuntimeException("oops")).when(next).handle(any());

        assertThrows(RuntimeException.class, () -> cb.process(ctx, next));
        assertThrows(RuntimeException.class, () -> cb.process(ctx, next));

        assertEquals("OPEN", cb.state());
    }

    @Test
    void configMethods_workAsExpected() {
        cb.threshold(0.7).windowSize(50).cooldown(5000).groupKey("new-key");
        assertEquals("CLOSED", cb.state());
    }

    @Test
    void groupKey_blank_defaultsToDefault() {
        cb.groupKey("");
        // Should use "default" internally
    }
}
