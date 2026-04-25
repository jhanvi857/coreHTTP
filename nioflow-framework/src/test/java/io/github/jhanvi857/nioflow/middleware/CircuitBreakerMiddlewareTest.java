package io.github.jhanvi857.nioflow.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.observability.RouteObservabilityRegistry;
import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CircuitBreakerMiddlewareTest {

    @AfterEach
    void reset() {
        RouteObservabilityRegistry.clearForTests();
    }

    @Test
    void opensAndRejectsWithRetryAfter() {
        NioFlowApp app = new NioFlowApp();
        CircuitBreakerMiddleware cb = new CircuitBreakerMiddleware().threshold(0.5d).windowSize(4).cooldown(1000);
        AtomicInteger calls = new AtomicInteger(0);

        app.group("/svc", group -> {
            group.use(cb);
            group.get("/work", ctx -> {
                int n = calls.incrementAndGet();
                if (n <= 2) {
                    throw new RuntimeException("downstream failure");
                }
                ctx.status(HttpStatus.OK).send("ok");
            });
        });

        HttpRequest req = new HttpRequest("/svc/work", "GET", "HTTP/1.1", Map.of(), new byte[0]);
        app.dispatch(req, null);
        app.dispatch(req, null);
        app.dispatch(req, null);
        app.dispatch(req, null);

        HttpContext rejected = app.dispatch(req, null);
        assertEquals(503, rejected.getResponse().getStatus().getCode());
        assertTrue(rejected.getResponse().getHeadersMap().containsKey("Retry-After"));
    }

    @Test
    void halfOpenProbeClosesOnSuccess() throws Exception {
        NioFlowApp app = new NioFlowApp();
        CircuitBreakerMiddleware cb = new CircuitBreakerMiddleware().threshold(1.0d).windowSize(2).cooldown(50);
        AtomicBoolean fail = new AtomicBoolean(true);

        app.group("/cb", group -> {
            group.use(cb);
            group.get("/probe", ctx -> {
                if (fail.get()) {
                    throw new RuntimeException("fail");
                }
                ctx.status(HttpStatus.OK).send("ok");
            });
        });

        HttpRequest req = new HttpRequest("/cb/probe", "GET", "HTTP/1.1", Map.of(), new byte[0]);
        app.dispatch(req, null);
        app.dispatch(req, null);

        HttpContext openReject = app.dispatch(req, null);
        assertEquals(503, openReject.getResponse().getStatus().getCode());

        Thread.sleep(70);
        fail.set(false);

        HttpContext probe = app.dispatch(req, null);
        assertEquals(200, probe.getResponse().getStatus().getCode());
        assertEquals("CLOSED", cb.state());
    }
}
