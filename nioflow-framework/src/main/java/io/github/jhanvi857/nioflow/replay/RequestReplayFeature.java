package io.github.jhanvi857.nioflow.replay;

import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import io.github.jhanvi857.nioflow.routing.Router;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * In-memory request replay facility guarded by NIOFLOW_REPLAY_ENABLED.
 */
public class RequestReplayFeature {
    private static final String[] SENSITIVE_HEADERS = new String[] {
            "authorization", "cookie", "x-api-key",
            "x-auth-token", "x-session-id", "proxy-authorization",
            "set-cookie", "x-csrf-token"
    };

    private final int capacity;
    private final ReplayEntry[] ring;
    private int cursor;
    private int count;

    public RequestReplayFeature(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.ring = new ReplayEntry[this.capacity];
    }

    public RouteHandler middleware(RouteHandler next) {
        return ctx -> {
            long start = System.nanoTime();
            next.handle(ctx);
            long elapsedMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            record(ctx, elapsedMs);
        };
    }

    public synchronized void record(HttpContext ctx, long responseTimeMs) {
        HttpRequest req = ctx.getRequest();
        ReplayEntry entry = new ReplayEntry();
        entry.timestamp = Instant.now().toString();
        entry.method = req.getMethod();
        entry.path = req.getPath();
        entry.headers = sanitizeHeaders(req.getHeaders());
        entry.body = req.getBodyAsString();
        entry.responseStatus = ctx.getResponse().getStatus().getCode();
        entry.responseBody = new String(ctx.getResponse().getBody(), java.nio.charset.StandardCharsets.UTF_8);
        entry.responseTimeMs = responseTimeMs;

        ring[cursor] = entry;
        cursor = (cursor + 1) % capacity;
        if (count < capacity) {
            count++;
        }
    }

    public synchronized List<Map<String, Object>> dump() {
        List<Map<String, Object>> out = new ArrayList<>();
        int start = (cursor - count + capacity) % capacity;
        for (int i = 0; i < count; i++) {
            int idx = (start + i) % capacity;
            ReplayEntry e = ring[idx];
            if (e == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("index", idx);
            row.put("timestamp", e.timestamp);
            row.put("method", e.method);
            row.put("path", e.path);
            row.put("headers", e.headers);
            row.put("body", e.body);
            row.put("responseStatus", e.responseStatus);
            row.put("responseTimeMs", e.responseTimeMs);
            out.add(row);
        }
        return out;
    }

    public synchronized ReplayEntry at(int index) {
        if (index < 0 || index >= capacity) {
            return null;
        }
        return ring[index];
    }

    public Map<String, Object> replayIndex(Router router, int index, java.util.concurrent.ExecutorService routeExecutor) {
        ReplayEntry entry;
        synchronized (this) {
            entry = at(index);
            if (entry == null) {
                return Map.of("error", "Replay entry not found", "index", index);
            }
        }

        HttpRequest replayReq = new HttpRequest(
                entry.path,
                entry.method,
                "HTTP/1.1",
                new HashMap<>(entry.headers),
                entry.body != null ? entry.body.getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0]);

        HttpContext replayCtx = router.dispatch(replayReq, routeExecutor);

        Map<String, Object> original = new HashMap<>();
        original.put("status", entry.responseStatus);
        original.put("body", entry.responseBody);
        original.put("responseTimeMs", entry.responseTimeMs);

        Map<String, Object> now = new HashMap<>();
        now.put("status", replayCtx.getResponse().getStatus().getCode());
        now.put("body", new String(replayCtx.getResponse().getBody(), java.nio.charset.StandardCharsets.UTF_8));

        Map<String, Object> result = new HashMap<>();
        result.put("index", index);
        result.put("method", entry.method);
        result.put("path", entry.path);
        result.put("original", original);
        result.put("current", now);
        return result;
    }

    private static Map<String, String> sanitizeHeaders(Map<String, String> headers) {
        Map<String, String> sanitized = new HashMap<>();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            String key = e.getKey();
            if (isSensitive(key)) {
                continue;
            }
            sanitized.put(key, e.getValue());
        }
        return sanitized;
    }

    private static boolean isSensitive(String key) {
        String lower = key == null ? "" : key.toLowerCase(Locale.ROOT);
        for (String blocked : SENSITIVE_HEADERS) {
            if (blocked.equals(lower)) {
                return true;
            }
        }
        return false;
    }

    public static class ReplayEntry {
        public String timestamp;
        public String method;
        public String path;
        public Map<String, String> headers;
        public String body;
        public int responseStatus;
        public String responseBody;
        public long responseTimeMs;
    }
}
