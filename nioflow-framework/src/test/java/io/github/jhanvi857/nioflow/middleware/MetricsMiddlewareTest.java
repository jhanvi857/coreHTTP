package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import io.github.jhanvi857.nioflow.observability.MetricsRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MetricsMiddlewareTest {
    private MetricsMiddleware middleware;
    private HttpContext ctx;
    private HttpResponse res;
    private RouteHandler next;
    private AtomicBoolean nextCalled;

    @BeforeEach
    void setUp() {
        middleware = new MetricsMiddleware();
        ctx = mock(HttpContext.class);
        res = mock(HttpResponse.class);
        nextCalled = new AtomicBoolean(false);
        next = (c) -> nextCalled.set(true);

        when(ctx.method()).thenReturn("GET");
        when(ctx.path()).thenReturn("/api/v1/users/123");
        when(ctx.getResponse()).thenReturn(res);
        when(res.getStatus()).thenReturn(HttpStatus.OK);
    }

    @Test
    void handle_recordsRequestToRegistry() throws Exception {
        double before = getCounterValue("nioflow_requests_total");
        middleware.process(ctx, next);
        double after = getCounterValue("nioflow_requests_total");
        assertEquals(before + 1, after);
    }

    @Test
    void handle_recordsLatency() throws Exception {
        middleware.process(ctx, next);
        String report = MetricsRegistry.scrape();
        assertTrue(report.contains("nioflow_request_latency"));
        assertTrue(report.contains("path=\"/api/v1/users/{id}\""));
        assertTrue(report.contains("method=\"GET\""));
        assertTrue(report.contains("status=\"200\""));
    }

    @Test
    void handle_alwaysCallsNext() throws Exception {
        middleware.process(ctx, next);
        assertTrue(nextCalled.get());
    }

    @Test
    void handle_errorStatusCode_incrementsErrorCounter() throws Exception {
        when(res.getStatus()).thenReturn(HttpStatus.NOT_FOUND);
        double before = getCounterValue("nioflow_errors_total");
        middleware.process(ctx, next);
        double after = getCounterValue("nioflow_errors_total");
        assertEquals(before + 1, after);
    }

    @Test
    void handle_nextThrows_metricStillRecorded() throws Exception {
        RouteHandler errorNext = (c) -> { throw new RuntimeException("Metric Error"); };
        double before = getCounterValue("nioflow_errors_total");
        
        assertThrows(RuntimeException.class, () -> middleware.process(ctx, errorNext));
        
        double after = getCounterValue("nioflow_errors_total");
        assertEquals(before + 1, after);
    }

    @Test
    void handle_differentRoutes_separateTagsInReport() throws Exception {
        when(ctx.path()).thenReturn("/api/v1/users/123");
        middleware.process(ctx, next);
        
        when(ctx.path()).thenReturn("/api/v1/products/456");
        middleware.process(ctx, next);
        
        String report = MetricsRegistry.scrape();
        assertTrue(report.contains("path=\"/api/v1/users/{id}\""));
        assertTrue(report.contains("path=\"/api/v1/products/{id}\""));
    }

    private double getCounterValue(String name) {
        return MetricsRegistry.getRegistry().get(name).counter().count();
    }
}
