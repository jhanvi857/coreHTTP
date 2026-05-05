package io.github.jhanvi857.nioflow.plugin;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class HealthCheckPluginTest {
    private static NioFlowApp app;
    private static int port;
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void setUp() throws Exception {
        app = new NioFlowApp();
        app.register(new HealthCheckPlugin());
        // Manually register ready check for testing
        app.get("/_ready", ctx -> ctx.status(HttpStatus.OK).send("READY"));
        
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
    }

    @Test
    public void getHealth_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/_health"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getHealth_bodyContainsStatusUp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/_health"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.body().contains("\"status\": \"UP\""));
    }

    @Test
    public void getHealth_bodyContainsMemoryUsed() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/_health"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.body().contains("\"memory_used_mb\""));
    }

    @Test
    public void getReady_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/_ready"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("READY", response.body());
    }
}
