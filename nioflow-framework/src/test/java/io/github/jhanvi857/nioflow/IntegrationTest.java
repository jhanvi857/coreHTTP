package io.github.jhanvi857.nioflow;

import io.github.jhanvi857.nioflow.middleware.CircuitBreakerMiddleware;
import io.github.jhanvi857.nioflow.middleware.LoggerMiddleware;
import io.github.jhanvi857.nioflow.middleware.RateLimitMiddleware;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {
    private NioFlowApp app;
    private int port;
    private HttpClient client;

    @BeforeAll
    void setup() throws InterruptedException {
        app = new NioFlowApp();
        
        app.use(new LoggerMiddleware());
        
        app.get("/hello", ctx -> ctx.send("world"));
        
        app.group("/api", group -> {
            group.use((ctx, next) -> {
                ctx.header("X-Test-Order", "first");
                next.handle(ctx);
            });
            group.get("/test", ctx -> ctx.send("ok"));
        });

        // Circuit Breaker test: 2 failures to open (threshold 0.5, window 2)
        CircuitBreakerMiddleware cb = new CircuitBreakerMiddleware()
                .groupKey("test-cb")
                .threshold(0.5)
                .windowSize(2)
                .cooldown(1000);
        
        app.get("/cb", ctx -> {
            throw new RuntimeException("fail");
        }).use(cb);

        // Rate Limiter test
        app.get("/rate-limited", ctx -> ctx.send("ok"))
           .use(new RateLimitMiddleware(1, 5000)); // 1 request per 5 seconds

        new Thread(() -> app.listen(0)).start();

        // Wait for server to start
        int attempts = 0;
        while (app.getPort() == -1 && attempts < 100) {
            Thread.sleep(50);
            attempts++;
        }
        port = app.getPort();
        assertTrue(port > 0, "Server port should be greater than 0, but was " + port);
        System.out.println("Started test server on port: " + port);

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterAll
    void tearDown() {
        if (app != null) {
            app.drainAndStop(1, TimeUnit.SECONDS);
        }
    }

    @Test
    void testBasicRouting() throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/hello"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, response.statusCode());
        assertEquals("world", response.body());
    }

    @Test
    void test404() throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/unknown"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, response.statusCode());
    }

    @Test
    void test405() throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/hello"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(405, response.statusCode());
    }

    @Test
    void testMiddlewareOrder() throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/api/test"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("X-Test-Order").isPresent());
    }

    @Test
    void testRateLimiter() throws Exception {
        // First request OK
        HttpResponse<String> response1 = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/rate-limited"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, response1.statusCode());

        // Second request 429
        HttpResponse<String> response2 = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/rate-limited"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(429, response2.statusCode());
    }

    @Test
    void testCircuitBreakerTransitions() throws Exception {
        // CLOSED -> OPEN (2 failures)
        for (int i = 0; i < 2; i++) {
            client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/cb"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
            );
        }

        // Now it should be OPEN (503)
        HttpResponse<String> responseOpen = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/cb"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(503, responseOpen.statusCode());
        assertTrue(responseOpen.body().contains("Circuit Open"));

        // Wait for cooldown
        Thread.sleep(1200);

        // HALF_OPEN -> CLOSED (will fail again but transition happens on next success)
        // Wait, the requirement says HALF_OPEN after cooldown -> CLOSED on success.
        // Let's add a route that can succeed to test HALF_OPEN -> CLOSED.
        
        app.get("/cb-toggle", ctx -> {
            if (ctx.header("X-Fail") != null) throw new RuntimeException("fail");
            ctx.send("ok");
        }).use(new CircuitBreakerMiddleware().groupKey("toggle-cb").windowSize(1).cooldown(500));

        // Fail once to open
        client.send(HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + port + "/cb-toggle")).header("X-Fail", "true").GET().build(), HttpResponse.BodyHandlers.ofString());
        
        // Should be open
        assertEquals(503, client.send(HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + port + "/cb-toggle")).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        
        Thread.sleep(600);
        
        // Should be HALF_OPEN, send success
        HttpResponse<String> respSuccess = client.send(HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + port + "/cb-toggle")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respSuccess.statusCode());
        
        // Should be CLOSED now
        assertEquals(200, client.send(HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + port + "/cb-toggle")).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @Order(Integer.MAX_VALUE) // Run last
    void testGracefulShutdown() throws Exception {
        app.get("/slow", ctx -> {
            Thread.sleep(500);
            ctx.send("done");
        });

        // Start request in background
        java.util.concurrent.CompletableFuture<HttpResponse<String>> future = client.sendAsync(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + port + "/slow"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        Thread.sleep(100);
        
        // Stop server while request is in flight
        long start = System.currentTimeMillis();
        app.drainAndStop(2, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - start;

        // Request should still complete
        HttpResponse<String> response = future.get(3, TimeUnit.SECONDS);
        assertEquals(200, response.statusCode());
        assertEquals("done", response.body());
        assertTrue(duration >= 400, "Shutdown should have waited for request");
    }
}
