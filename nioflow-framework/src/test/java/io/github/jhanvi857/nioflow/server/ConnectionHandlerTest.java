package io.github.jhanvi857.nioflow.server;

import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionHandlerTest {

    private static NioFlowApp app;
    private static int port;

    @BeforeAll
    static void setUp() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        app = new NioFlowApp();
        app.get("/test", ctx -> ctx.status(HttpStatus.OK).send("ok"));
        new Thread(() -> app.listen(port)).start();

        long start = System.currentTimeMillis();
        while (app.getPort() == -1 && System.currentTimeMillis() - start < 2000) {
            Thread.sleep(50);
        }
    }

    @AfterAll
    static void tearDown() {
        if (app != null) {
            app.drainAndStop(500, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    void handle_malformedRequest_returns400() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write("NOT_A_VALID_HTTP_REQUEST\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.contains("400 Bad Request"));
        }
    }

    @Test
    void handle_crlfInHeader_returns400() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            // Using a single \r (not followed by \n) inside the value
            out.write("GET /test HTTP/1.1\r\nX-Injected: value\roops\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.contains("400 Bad Request"));
        }
    }

    @Test
    void handle_nullByteInPath_returns400() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write("GET /test\0bad HTTP/1.1\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.contains("400 Bad Request"));
        }
    }

    @Test
    void handle_bodyExceedsLimit_returns413() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            // 11MB Content-Length
            long largeSize = 11 * 1024 * 1024;
            out.write(("POST /test HTTP/1.1\r\nContent-Length: " + largeSize + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.contains("413 Payload Too Large"));
        }
    }

    @Test
    void handle_headersExceedLimit_returns431() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            StringBuilder largeHeader = new StringBuilder("GET /test HTTP/1.1\r\n");
            for (int i = 0; i < 1000; i++) {
                largeHeader.append("X-Header-").append(i).append(": some-long-value-to-fill-up-space-").append(i).append("\r\n");
            }
            largeHeader.append("\r\n");
            out.write(largeHeader.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.contains("431 Request Header Fields Too Large"));
        }
    }

    @Test
    void duplicateContentLength_returns400() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write("POST /test HTTP/1.1\r\nHost: localhost\r\nContent-Length: 5\r\nContent-Length: 10\r\n\r\nhello".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.startsWith("HTTP/1.1 400"));
        }
    }

    @Test
    void transferEncodingAndContentLength_returns400() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write("POST /test HTTP/1.1\r\nHost: localhost\r\nTransfer-Encoding: chunked\r\nContent-Length: 5\r\n\r\n5\r\nhello\r\n0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.startsWith("HTTP/1.1 400"));
        }
    }

    @Test
    void multiValueTransferEncoding_returns400() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write("POST /test HTTP/1.1\r\nHost: localhost\r\nTransfer-Encoding: identity, chunked\r\nContent-Length: 5\r\n\r\nhello".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.startsWith("HTTP/1.1 400"));
        }
    }

    @Test
    void handle_invalidTE_returns400() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write("POST /test HTTP/1.1\r\nTransfer-Encoding: gzip\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.contains("400 Bad Request"));
        }
    }

    @Test
    void handle_negativeContentLength_returns400() throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write("POST /test HTTP/1.1\r\nContent-Length: -5\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String response = readResponse(socket.getInputStream());
            assertTrue(response.contains("400 Bad Request"));
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
            // Ignore connection reset
        }
        return out.toString(StandardCharsets.UTF_8);
    }
}
