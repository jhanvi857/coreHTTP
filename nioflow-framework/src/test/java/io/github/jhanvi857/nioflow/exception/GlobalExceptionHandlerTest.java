package io.github.jhanvi857.nioflow.exception;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {
    private static NioFlowApp app;
    private static int port;
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void setUp() throws Exception {
        app = new NioFlowApp();
        
        app.get("/throw-mapped", ctx -> {
            throw new IllegalArgumentException("mapped error");
        });

        app.get("/throw-unmapped", ctx -> {
            throw new RuntimeException("unmapped error");
        });

        app.get("/custom-exception", ctx -> {
            throw new IllegalStateException("custom");
        });
        app.exception(IllegalStateException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        });

        app.post("/json-only", ctx -> {
            String type = ctx.header("Content-Type");
            if (type == null || !type.contains("application/json")) {
                throw new UnsupportedMediaTypeException("only json allowed");
            }
            ctx.send("ok");
        });

        new Thread(() -> app.listen(0)).start();
        while (app.getPort() == -1) Thread.sleep(50);
        port = app.getPort();
    }

    @AfterAll
    static void tearDown() {
        if (app != null) {
            app.drainAndStop(500, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void mappedException_correctStatusReturned() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/custom-exception"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("custom"));
    }

    @Test
    public void unmappedException_returns500() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/throw-unmapped"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode());
    }

    @Test
    public void errorDetailsSuppressed_stackTraceAbsent() throws Exception {
        setExposeDetails(false);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/throw-unmapped"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertFalse(response.body().contains("at io.github"));
        assertFalse(response.body().contains("Exception"));
        assertFalse(response.body().contains(".java:"));
    }

    @Test
    public void errorDetailsExposed_messagePresent() throws Exception {
        setExposeDetails(true);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/throw-mapped"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // GlobalExceptionHandler line 36 uses sanitizeForJson(message) if EXPOSE_ERROR_DETAILS is true
        assertTrue(response.body().contains("mapped error"));
    }

    @Test
    public void unsupportedMediaType_returns415() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/json-only"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(415, response.statusCode());
        assertFalse(response.body().contains("at io.github"));
    }

    @Test
    public void payloadTooLarge_returns413() throws Exception {
        // Send a request with a very large Content-Length to trigger HttpParser exception
        // ConnectionHandler should catch it and return 413.
        java.net.Socket socket = new java.net.Socket("localhost", port);
        java.io.OutputStream out = socket.getOutputStream();
        out.write("POST /ping HTTP/1.1\r\nContent-Length: 20000000\r\n\r\n".getBytes());
        out.flush();
        
        java.io.InputStream in = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int read = in.read(buffer);
        String response = new String(buffer, 0, read);
        assertTrue(response.contains("413 Payload Too Large"));
        socket.close();
    }

    @Test
    public void requestHeadersTooLarge_returns431() throws Exception {
        java.net.Socket socket = new java.net.Socket("localhost", port);
        java.io.OutputStream out = socket.getOutputStream();
        StringBuilder large = new StringBuilder("GET / HTTP/1.1\r\n");
        for (int i = 0; i < 200; i++) {
            large.append("X-Large-").append(i).append(": ").append("a".repeat(100)).append("\r\n");
        }
        large.append("\r\n");
        out.write(large.toString().getBytes());
        out.flush();
        
        java.io.InputStream in = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int read = in.read(buffer);
        String response = new String(buffer, 0, read);
        assertTrue(response.contains("431 Request Header Fields Too Large"));
        socket.close();
    }

    private void setExposeDetails(boolean value) throws Exception {
        Field field = GlobalExceptionHandler.class.getDeclaredField("EXPOSE_ERROR_DETAILS");
        field.setAccessible(true);
        field.set(null, value);
    }
}
