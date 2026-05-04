package io.github.jhanvi857.nioflow.server;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    private NioFlowApp app;
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        System.setProperty("nioflow.threads", "1");
        System.setProperty("nioflow.queueCapacity", "1");
        System.setProperty("nioflow.socketTimeoutMs", "1000");

        app = new NioFlowApp();
        app.get("/hello", ctx -> ctx.status(HttpStatus.OK).send("world"));
        app.get("/slow", ctx -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            ctx.status(HttpStatus.OK).send("slow");
        });

        new Thread(() -> app.listen(port)).start();

        long start = System.currentTimeMillis();
        while (app.getPort() == -1 && System.currentTimeMillis() - start < 2000) {
            Thread.sleep(50);
        }
    }

    @AfterEach
    void tearDown() {
        if (app != null) {
            app.drainAndStop(500, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    void server_startsAndAcceptsConnections() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write("GET /hello HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.contains("200 OK"), "Response should contain 200 OK, but was: " + response);
            assertTrue(response.contains("world"), "Response should contain world, but was: " + response);
        }
    }

    @Test
    void server_atCapacity_rejectsWith503() throws Exception {
        Socket s1 = new Socket("localhost", port);
        s1.getOutputStream().write("GET /slow HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        s1.getOutputStream().flush();

        Thread.sleep(100);

        Socket s2 = new Socket("localhost", port);
        s2.getOutputStream().write("GET /slow HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        s2.getOutputStream().flush();

        Thread.sleep(100);

        try (Socket s3 = new Socket("localhost", port)) {
            s3.setSoTimeout(2000);
            s3.getOutputStream().write("GET /hello HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            s3.getOutputStream().flush();

            String response = readResponse(s3.getInputStream());
            assertTrue(response.contains("503 Service Unavailable"), "Response should contain 503, but was: " + response);
        } finally {
            s1.close();
            s2.close();
        }
    }

    @Test
    void server_socketTimeout_closesConnection() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            Thread.sleep(1500);
            int read = socket.getInputStream().read();
            assertEquals(-1, read);
        }
    }

    private String readResponse(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        try {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (java.io.IOException e) {
            // Ignore connection reset/timeout after we have some data or if it's expected
        }
        return out.toString(StandardCharsets.UTF_8);
    }
}
