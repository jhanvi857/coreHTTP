package io.github.jhanvi857.nioflow.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.observability.RouteObservabilityRegistry;
import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RequestHedgingTest {

    @AfterEach
    void reset() {
        RouteObservabilityRegistry.clearForTests();
    }

    @Test
    void hedgeFiresWhenPrimaryIsSlow() {
        NioFlowApp app = new NioFlowApp();
        AtomicInteger calls = new AtomicInteger(0);

        app.get("/search", ctx -> {
            if (calls.incrementAndGet() == 1) {
                Thread.sleep(180);
            }
            ctx.send("ok");
        }).hedge(40);

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            HttpContext out = app.dispatch(new HttpRequest("/search", "GET", "HTTP/1.1", Map.of(), new byte[0]), executor);
            assertEquals(200, out.getResponse().getStatus().getCode());
            assertTrue(calls.get() >= 2);

            RouteObservabilityRegistry.RouteSnapshot snapshot = RouteObservabilityRegistry.statsFor("GET /search").snapshot();
            assertTrue(snapshot.hedgeCount >= 1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void hedgeDoesNotFireForFastHandler() {
        NioFlowApp app = new NioFlowApp();
        AtomicInteger calls = new AtomicInteger(0);

        app.get("/fast", ctx -> {
            calls.incrementAndGet();
            ctx.send("ok");
        }).hedge(100);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            HttpContext out = app.dispatch(new HttpRequest("/fast", "GET", "HTTP/1.1", Map.of(), new byte[0]), executor);
            assertEquals(200, out.getResponse().getStatus().getCode());
            assertEquals(1, calls.get());

            RouteObservabilityRegistry.RouteSnapshot snapshot = RouteObservabilityRegistry.statsFor("GET /fast").snapshot();
            assertEquals(0, snapshot.hedgeCount);
        } finally {
            executor.shutdownNow();
        }
    }
}
