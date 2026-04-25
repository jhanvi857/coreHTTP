package io.github.jhanvi857.nioflow.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ChaosMiddlewareTest {

    @AfterEach
    void clear() {
        System.clearProperty("NIOFLOW_CHAOS_ENABLED");
    }

    @Test
    void chaosDisabledSkipsInjection() throws Exception {
        System.setProperty("NIOFLOW_CHAOS_ENABLED", "false");
        ChaosMiddleware middleware = new ChaosMiddleware().error(500, 1.0d);
        AtomicBoolean called = new AtomicBoolean(false);

        HttpContext ctx = new HttpContext(new HttpRequest("/a", "GET", "HTTP/1.1", Map.of(), new byte[0]));
        middleware.process(ctx, c -> {
            called.set(true);
            c.status(HttpStatus.OK).send("ok");
        });

        assertTrue(called.get());
        assertEquals(200, ctx.getResponse().getStatus().getCode());
    }

    @Test
    void chaosErrorAndDropInjectWhenEnabled() throws Exception {
        System.setProperty("NIOFLOW_CHAOS_ENABLED", "true");

        ChaosMiddleware error = new ChaosMiddleware().error(503, 1.0d);
        AtomicBoolean nextCalled = new AtomicBoolean(false);
        HttpContext errorCtx = new HttpContext(new HttpRequest("/b", "GET", "HTTP/1.1", Map.of(), new byte[0]));
        error.process(errorCtx, c -> nextCalled.set(true));
        assertFalse(nextCalled.get());
        assertEquals(503, errorCtx.getResponse().getStatus().getCode());

        ChaosMiddleware drop = new ChaosMiddleware().drop(1.0d);
        HttpContext dropCtx = new HttpContext(new HttpRequest("/c", "GET", "HTTP/1.1", Map.of(), "body".getBytes(StandardCharsets.UTF_8)));
        drop.process(dropCtx, c -> {
            throw new IllegalStateException("must not be called");
        });
        assertTrue(dropCtx.isDropResponse());
    }
}
