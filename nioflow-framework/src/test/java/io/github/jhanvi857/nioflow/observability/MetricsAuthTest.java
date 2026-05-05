package io.github.jhanvi857.nioflow.observability;

import io.github.jhanvi857.nioflow.NioFlowApp;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class MetricsAuthTest {
    private static final HttpClient client = HttpClient.newHttpClient();

    @Test
    public void getMetrics_withToken_correctToken_returns200() throws Exception {
        System.setProperty("NIOFLOW_METRICS_TOKEN", "secret123");
        NioFlowApp app = new NioFlowApp();
        app.enableMetrics();
        
        new Thread(() -> app.listen(0)).start();
        while (app.getPort() == -1) Thread.sleep(50);
        int port = app.getPort();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/metrics"))
                    .header("Authorization", "Bearer secret123")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
        } finally {
            app.drainAndStop(500, TimeUnit.MILLISECONDS);
            System.clearProperty("NIOFLOW_METRICS_TOKEN");
        }
    }

    @Test
    public void getMetrics_withToken_wrongToken_returns401() throws Exception {
        System.setProperty("NIOFLOW_METRICS_TOKEN", "secret123");
        NioFlowApp app = new NioFlowApp();
        app.enableMetrics();
        
        new Thread(() -> app.listen(0)).start();
        while (app.getPort() == -1) Thread.sleep(50);
        int port = app.getPort();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/metrics"))
                    .header("Authorization", "Bearer wrong")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(401, response.statusCode());
        } finally {
            app.drainAndStop(500, TimeUnit.MILLISECONDS);
            System.clearProperty("NIOFLOW_METRICS_TOKEN");
        }
    }

    @Test
    public void getMetrics_withToken_noHeader_returns401() throws Exception {
        System.setProperty("NIOFLOW_METRICS_TOKEN", "secret123");
        NioFlowApp app = new NioFlowApp();
        app.enableMetrics();
        
        new Thread(() -> app.listen(0)).start();
        while (app.getPort() == -1) Thread.sleep(50);
        int port = app.getPort();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/metrics"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(401, response.statusCode());
        } finally {
            app.drainAndStop(500, TimeUnit.MILLISECONDS);
            System.clearProperty("NIOFLOW_METRICS_TOKEN");
        }
    }
}
