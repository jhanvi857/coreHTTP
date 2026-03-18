package com.jhanvi857.taskplanner;

import com.jhanvi857.nioflow.NioFlowApp;
import com.jhanvi857.nioflow.protocol.HttpStatus;
import com.jhanvi857.taskplanner.controller.TaskController;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that start a real NioFlow server on a random port and verify
 * auth enforcement and observability routes without a live database.
 *
 * JWT_SECRET is supplied as a system property by the test setup so the startup
 * validation in DemoApplication passes. Setting NIOFLOW_ENABLE_DB=false skips
 * all JDBC initialization.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskPlannerIntegrationTest {

    private static NioFlowApp app;
    private static int port;
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @BeforeAll
    static void startServer() throws Exception {
        System.setProperty("nioflow.jwtSecret", "integration-test-secret-please-change-in-prod!!");
        System.setProperty("nioflow.enableDB", "false");

        port = findFreePort();
        app = buildTestApp();

        Thread serverThread = new Thread(() -> app.listen(port));
        serverThread.setDaemon(true);
        serverThread.start();

        awaitServerReady(port, 5_000);
    }

    @AfterAll
    static void stopServer() {
        app.drainAndStop(5, TimeUnit.SECONDS);
    }

    @Test
    @Order(1)
    void healthEndpoint_returns200_withStatusUp() throws Exception {
        var resp = get("/_health");
        assertEquals(200, resp.statusCode(), "/_health should return 200");
        assertTrue(resp.body().contains("UP"), "Expected 'UP' in health body: " + resp.body());
    }

    @Test
    @Order(2)
    void metricsEndpoint_returns200() throws Exception {
        var resp = get("/metrics");
        assertEquals(200, resp.statusCode(), "/metrics should return 200");
    }

    @Test
    @Order(3)
    void readinessEndpoint_returns200_whenDbDisabled() throws Exception {
        var resp = get("/_ready");
        assertEquals(200, resp.statusCode(), "/_ready should return 200 when DB is disabled");
        assertTrue(resp.body().contains("DISABLED"), "Expected DB disabled state in readiness response");
    }

    @Test
    @Order(4)
    void taskList_withoutAuth_returns401() throws Exception {
        assertEquals(401, get("/api/tasks/").statusCode());
    }

    @Test
    @Order(5)
    void createTask_withoutAuth_returns401() throws Exception {
        var req = HttpRequest.newBuilder(uri("/api/tasks/"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"test\"}"))
                .header("Content-Type", "application/json")
                .build();
        assertEquals(401, HTTP.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @Order(6)
    void getTaskById_withoutAuth_returns401() throws Exception {
        assertEquals(401, get("/api/tasks/1").statusCode());
    }

    @Test
    @Order(7)
    void deleteTask_withoutAuth_returns401() throws Exception {
        var req = HttpRequest.newBuilder(uri("/api/tasks/1"))
                .DELETE()
                .build();
        assertEquals(401, HTTP.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @Order(8)
    void taskList_withInvalidToken_returns401() throws Exception {
        var req = HttpRequest.newBuilder(uri("/api/tasks/"))
                .GET()
                .header("Authorization", "Bearer this.is.not.a.valid.token")
                .build();
        assertEquals(401, HTTP.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @Order(9)
    void taskList_withMalformedAuthHeader_returns401() throws Exception {
        var req = HttpRequest.newBuilder(uri("/api/tasks/"))
                .GET()
                .header("Authorization", "NotBearer somevalue")
                .build();
        assertEquals(401, HTTP.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }


    @Test
    @Order(10)
    void secureEndpoint_withoutAuth_returns401() throws Exception {
        assertEquals(401, get("/api/secure/").statusCode());
    }

    @Test
    @Order(11)
    void secureEndpoint_withInvalidToken_returns401() throws Exception {
        var req = HttpRequest.newBuilder(uri("/api/secure/"))
                .GET()
                .header("Authorization", "Bearer bad.token.value")
                .build();
        assertEquals(401, HTTP.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @Order(12)
    void internalServerError_doesNotLeakExceptionMessage() throws Exception {
        var resp = get("/boom");
        assertEquals(500, resp.statusCode());
        assertTrue(resp.body().contains("Internal Server Error"));
        assertFalse(resp.body().contains("boom-message"));
    }


    /**
     * Builds a test NioFlowApp with the same route topology as DemoApplication
     * but without static file serving (avoids filesystem dependency in tests).
     */
    private static NioFlowApp buildTestApp() {
        NioFlowApp testApp = new NioFlowApp();
        testApp.use(new com.jhanvi857.nioflow.middleware.LoggerMiddleware());
        testApp.use(new com.jhanvi857.nioflow.middleware.MetricsMiddleware());
        testApp.use(new com.jhanvi857.nioflow.middleware.CorsMiddleware("http://localhost:3000"));
        testApp.use(new com.jhanvi857.nioflow.middleware.RateLimitMiddleware(1000, 10_000));

        testApp.register(new com.jhanvi857.nioflow.plugin.HealthCheckPlugin());
        testApp.get("/_ready", ctx ->
            ctx.status(HttpStatus.OK).json(java.util.Map.of("status", "UP", "database", "DISABLED"))
        );
        testApp.get("/metrics", ctx ->
            ctx.status(HttpStatus.OK).send(
                com.jhanvi857.nioflow.middleware.MetricsMiddleware.getMetricsReport())
        );
        testApp.get("/boom", ctx -> {
            throw new RuntimeException("boom-message");
        });

        testApp.group("/api/tasks", tasks -> {
            tasks.use(new com.jhanvi857.nioflow.middleware.AuthMiddleware());
            TaskController taskController = new TaskController();
            tasks.get("/", taskController::list);
            tasks.post("/", taskController::create);
            tasks.get("/:id", taskController::get);
            tasks.delete("/:id", taskController::delete);
        });

        testApp.group("/api/secure", secure -> {
            secure.use(new com.jhanvi857.nioflow.middleware.AuthMiddleware());
            secure.get("/", ctx -> {
                String user = ctx.header("X-Auth-User");
                ctx.status(HttpStatus.OK).json(java.util.Map.of("message", "Hello, " + user));
            });
        });

        testApp.onError((err, ctx) ->
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .json(java.util.Map.of("error", "Internal Server Error"))
        );

        return testApp;
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            s.setReuseAddress(true);
            return s.getLocalPort();
        }
    }

    private static void awaitServerReady(int port, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try (Socket ignored = new Socket("localhost", port)) {
                return;
            } catch (IOException e) {
                Thread.sleep(100);
            }
        }
        throw new IllegalStateException(
            "Server did not become ready on port " + port + " within " + timeoutMs + "ms");
    }

    private HttpResponse<String> get(String path) throws Exception {
        return HTTP.send(
            HttpRequest.newBuilder(uri(path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
