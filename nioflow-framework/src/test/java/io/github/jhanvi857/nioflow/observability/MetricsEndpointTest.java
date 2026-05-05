package io.github.jhanvi857.nioflow.observability;

import io.github.jhanvi857.nioflow.Env;
import io.github.jhanvi857.nioflow.NioFlowApp;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MetricsEndpointTest {
    private static NioFlowApp app;
    private static int port;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static MockedStatic<Env> mockedEnv;

    @BeforeAll
    static void setUp() throws Exception {
        mockedEnv = mockStatic(Env.class);
        // Default: no token
        mockedEnv.when(() -> Env.get("NIOFLOW_METRICS_TOKEN")).thenReturn(null);

        app = new NioFlowApp();
        app.get("/ping", ctx -> ctx.send("pong"));
        app.enableMetrics();
        
        new Thread(() -> app.listen(0)).start();
        
        long start = System.currentTimeMillis();
        while (app.getPort() == -1 && System.currentTimeMillis() - start < 5000) {
            Thread.sleep(100);
        }
        port = app.getPort();
    }

    @AfterAll
    static void tearDown() {
        if (app != null) {
            app.drainAndStop(500, TimeUnit.MILLISECONDS);
        }
        mockedEnv.close();
        RouteObservabilityRegistry.clearForTests();
    }

    @Test
    public void getMetrics_noToken_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/metrics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getMetrics_responseContainsRouteCounters() throws Exception {
        // Make some requests to populate metrics
        for (int i = 0; i < 3; i++) {
            client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/ping")).GET().build(), HttpResponse.BodyHandlers.ofString());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/metrics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.body().contains("GET /ping"));
        assertTrue(response.body().contains("requests="));
    }

    @Test
    public void getMetrics_responseContainsCircuitBreakerState() throws Exception {
        RouteObservabilityRegistry.registerCircuitState("test-group", () -> "CLOSED");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/metrics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.body().contains("group=test-group state=CLOSED"));
    }
}
