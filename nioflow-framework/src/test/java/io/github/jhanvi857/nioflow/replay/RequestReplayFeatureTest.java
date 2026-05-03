package io.github.jhanvi857.nioflow.replay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.auth.JwtProvider;
import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RequestReplayFeatureTest {

    @BeforeAll
    static void initJwt() {
        // Must set JWT_SECRET before JwtProvider class loads
        System.setProperty("nioflow.jwtSecret", "test-secret-key-that-is-definitely-long-enough-for-hmac-sha256!!");
    }

    @AfterAll
    static void clear() {
        System.clearProperty("NIOFLOW_REPLAY_ENABLED");
        System.clearProperty("nioflow.jwtSecret");
    }

    @Test
    void recordsRequestsAndStripsSensitiveHeaders() {
        System.setProperty("NIOFLOW_REPLAY_ENABLED", "true");

        NioFlowApp app = new NioFlowApp();
        app.enableReplay(5);
        app.post("/echo", ctx -> ctx.status(HttpStatus.CREATED).json(Map.of("ok", true)));

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer secret");
        headers.put("Cookie", "id=123");
        headers.put("X-API-Key", "abc");
        headers.put("X-Trace-Id", "trace-1");

        app.dispatch(new HttpRequest("/echo", "POST", "HTTP/1.1", headers, "payload".getBytes(StandardCharsets.UTF_8)), null);

        // /_replay is now auth-gated — provide a valid JWT
        String token = JwtProvider.generateToken("test-user", "admin");
        Map<String, String> authHeaders = new HashMap<>();
        authHeaders.put("Authorization", "Bearer " + token);

        HttpContext replayList = app.dispatch(new HttpRequest("/_replay", "GET", "HTTP/1.1", authHeaders, new byte[0]), null);

        String body = new String(replayList.getResponse().getBody(), StandardCharsets.UTF_8);
        assertTrue(body.contains("/echo"));
        assertTrue(body.contains("X-Trace-Id"));
        assertFalse(body.toLowerCase().contains("\"authorization\""));
        assertFalse(body.toLowerCase().contains("\"cookie\""));
        assertFalse(body.toLowerCase().contains("\"x-api-key\""));
    }

    @Test
    void replayEndpointReturnsOriginalAndCurrent() {
        System.setProperty("NIOFLOW_REPLAY_ENABLED", "true");

        NioFlowApp app = new NioFlowApp();
        app.enableReplay(5);
        AtomicInteger version = new AtomicInteger(0);

        app.get("/version", ctx -> ctx.status(HttpStatus.OK).json(Map.of("v", version.incrementAndGet())));

        app.dispatch(new HttpRequest("/version", "GET", "HTTP/1.1", Map.of(), new byte[0]), null);

        // Provide a valid JWT for the auth-gated /_replay/:index endpoint
        String token = JwtProvider.generateToken("test-user", "admin");
        Map<String, String> authHeaders = new HashMap<>();
        authHeaders.put("Authorization", "Bearer " + token);

        HttpContext replayRun = app.dispatch(new HttpRequest("/_replay/0", "POST", "HTTP/1.1", authHeaders, new byte[0]), null);

        String body = new String(replayRun.getResponse().getBody(), StandardCharsets.UTF_8);
        assertTrue(body.contains("\"original\""));
        assertTrue(body.contains("\"current\""));
        assertTrue(body.contains("\"status\":200"));
    }
}
