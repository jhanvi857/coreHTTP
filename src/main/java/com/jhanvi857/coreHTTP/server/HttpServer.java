package com.jhanvi857.coreHTTP.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;

public class HttpServer {
    private final int port;
    private final ExecutorService threadPool;
    private final int socketReadTimeoutMs;

    public HttpServer(int port) {
        this.port = port;
        int workerThreads = readIntSetting("corehttp.threads", "COREHTTP_THREADS", 10, 1);
        int queueCapacity = readIntSetting("corehttp.queueCapacity", "COREHTTP_QUEUE_CAPACITY", 100, 1);
        this.socketReadTimeoutMs = readIntSetting("corehttp.socketTimeoutMs", "COREHTTP_SOCKET_TIMEOUT_MS", 15000, 1000);

        // Why this change:
        // FixedThreadPool uses an unbounded queue by default. Under overload, memory can grow endlessly.
        // Bounded queue gives us controlled backpressure and lets us reject quickly with 503.
        this.threadPool = new ThreadPoolExecutor(
                workerThreads,
                workerThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity));

        System.out.println("Thread pool workers: " + workerThreads + ", queue capacity: " + queueCapacity);
        System.out.println("Socket read timeout (ms): " + socketReadTimeoutMs);
    }

    public void start(com.jhanvi857.coreHTTP.routing.Router router) {
        System.out.println("Starting TCP server on port : " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Waiting for the client...");
                Socket clienSocket = serverSocket.accept();
                System.out.println("Client accepted.");

                // Slow-client defense: read operations now fail fast instead of blocking forever.
                clienSocket.setSoTimeout(socketReadTimeoutMs);

                ConnectionHandler handler = new ConnectionHandler(clienSocket, router);

                // Backpressure behavior: queue saturation throws rejection and we respond with 503.
                try {
                    threadPool.execute(handler);
                } catch (RejectedExecutionException rejected) {
                    sendServiceUnavailable(clienSocket);
                }
            }
        } catch (IOException e) {
            System.out.println("Server failed ! " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        com.jhanvi857.coreHTTP.routing.Router router = new com.jhanvi857.coreHTTP.routing.Router();

        // Reusable static root selection:
        // 1. JVM property: -Dcorehttp.staticDir=...
        // 2. env var: COREHTTP_STATIC_DIR
        // 3. fallback to bundled demo public folder
        String staticDir = System.getProperty("corehttp.staticDir");
        if (staticDir == null || staticDir.isBlank()) {
            staticDir = System.getenv("COREHTTP_STATIC_DIR");
        }
        if (staticDir == null || staticDir.isBlank()) {
            staticDir = resolveDefaultStaticDir();
        }
        System.out.println("Serving static files from: " + staticDir);

        // static file handler for frontend assets
        router.register("/", new StaticFileHandler(staticDir));
        router.register("/hello", request -> new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                com.jhanvi857.coreHTTP.protocol.HttpStatus.OK, "Hello from Router!"));

        new HttpServer(8080).start(router);
    }

    private static String resolveDefaultStaticDir() {
        // Why need ?
        // coz The server may be launched from project root, scripts folder, IDE, or CI runners and a single relative path breaks in some of those cases and causes 404 on '/'.
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path[] candidates = new Path[] {
                cwd.resolve("src/main/resources/public").normalize(),
                cwd.resolve("../src/main/resources/public").normalize(),
                cwd.resolve("target/public").normalize(),
                cwd.resolve("../target/public").normalize()
        };

        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate.toString();
            }
        }

        // fallback for environments where assets are external..will override with -Dcorehttp.staticDir or COREHTTP_STATIC_DIR.
        return cwd.resolve("src/main/resources/public").normalize().toString();
    }

    private void sendServiceUnavailable(Socket socket) {
        try {
            HttpResponse response = new HttpResponse(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "<h1>503 Service Unavailable</h1><p>Server is busy. Please retry shortly.</p>");
            response.writeTo(socket.getOutputStream());
        } catch (IOException ignored) {
            System.out.println("Failed to send 503 response: " + ignored.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
                System.out.println("Failed to close overloaded socket: " + ignored.getMessage());
            }
        }
    }

    private static int readIntSetting(String propertyKey, String envKey, int defaultValue, int minValue) {
        String configured = System.getProperty(propertyKey);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(envKey);
        }

        if (configured == null || configured.isBlank()) {
            return defaultValue;
        }

        try {
            int parsed = Integer.parseInt(configured.trim());
            if (parsed < minValue) {
                return defaultValue;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}