package io.github.jhanvi857.nioflow.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.observability.RouteObservabilityRegistry;
import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RouteObservabilityFluentApiTest {

    @AfterEach
    void reset() {
        RouteObservabilityRegistry.clearForTests();
    }

    @Test
    void timeoutReturnsRequestTimeout() {
        NioFlowApp app = new NioFlowApp();
        app.get("/slow", ctx -> {
            Thread.sleep(120);
            ctx.status(HttpStatus.OK).send("ok");
        }).timeout(50);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            HttpContext out = app.dispatch(new HttpRequest("/slow", "GET", "HTTP/1.1", Map.of(), new byte[0]), executor);
            assertEquals(408, out.getResponse().getStatus().getCode());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void perRouteRateLimitIsIndependent() {
        NioFlowApp app = new NioFlowApp();
        app.get("/limited", ctx -> ctx.status(HttpStatus.OK).send("ok")).rateLimit(1, 10_000);

        HttpRequest req = new HttpRequest("/limited", "GET", "HTTP/1.1", Map.of(), new byte[0]);
        HttpContext first = app.dispatch(req, null);
        HttpContext second = app.dispatch(req, null);

        assertEquals(200, first.getResponse().getStatus().getCode());
        assertEquals(429, second.getResponse().getStatus().getCode());

        RouteObservabilityRegistry.RouteSnapshot snapshot = RouteObservabilityRegistry.statsFor("GET /limited").snapshot();
        assertEquals(2, snapshot.requestCount);
        assertTrue(snapshot.errorCount >= 1);
    }
}
