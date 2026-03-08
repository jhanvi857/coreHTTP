package com.jhanvi857.coreHTTP.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private final int port;
    private final ThreadPoolExecutor threadPool; // Changed to ThreadPoolExecutor for shutdown control
    private final int socketReadTimeoutMs;
    private volatile boolean running = false;

    public HttpServer(int port) {
        this.port = port;
        int workerThreads = readIntSetting("corehttp.threads", "COREHTTP_THREADS", 10, 1);
        int queueCapacity = readIntSetting("corehttp.queueCapacity", "COREHTTP_QUEUE_CAPACITY", 100, 1);
        this.socketReadTimeoutMs = readIntSetting("corehttp.socketTimeoutMs", "COREHTTP_SOCKET_TIMEOUT_MS", 15000,
                1000);

        // Why this change:
        // FixedThreadPool uses an unbounded queue by default. Under overload, memory
        // can grow endlessly.
        // Bounded queue gives controlled backpressure and lets us reject quickly
        // with 503.
        this.threadPool = new ThreadPoolExecutor(
                workerThreads,
                workerThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity));

        logger.info("Thread pool initialized with {} workers and queue capacity of {}", workerThreads, queueCapacity);
        logger.info("Socket read timeout set to {}ms", socketReadTimeoutMs);
    }

    public void start(com.jhanvi857.coreHTTP.routing.Router router) {
        this.running = true;

        // Registering a shutdown hook to handle SIGTERM (Ctrl+C).
        // This ensures in-flight requests finish before the JVM exits.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received. Starting graceful shutdown...");
            this.running = false;

            // 1. Stop accepting new connections at the pool level
            threadPool.shutdown();
            try {
                // 2. Wait for current tasks to finish -30 sec
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("Forcing immediate shutdown - some requests did not finish in time.");
                    threadPool.shutdownNow();
                }
                logger.info("CoreHTTP server stopped successfully.");
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));

        logger.info("Starting CoreHTTP TCP server on port {}", port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Only accept new connections while the 'running' flag is true
            while (running) {
                serverSocket.setSoTimeout(1000);
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                } catch (java.net.SocketTimeoutException e) {
                    continue;
                }

                logger.info("Accepted connection from {}", clientSocket.getRemoteSocketAddress());
                clientSocket.setSoTimeout(socketReadTimeoutMs);

                ConnectionHandler handler = new ConnectionHandler(clientSocket, router);

                try {
                    threadPool.execute(handler);
                } catch (RejectedExecutionException rejected) {
                    logger.warn("Server overloaded or shutting down! Rejecting {}",
                            clientSocket.getRemoteSocketAddress());
                    sendServiceUnavailable(clientSocket);
                }
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Critical server failure: {}", e.getMessage(), e);
            }
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
        logger.info("Serving static assets from: {}", staticDir);

        // static file handler for frontend assets
        router.register("/", new StaticFileHandler(staticDir));
        router.register("/_health", request -> new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                com.jhanvi857.coreHTTP.protocol.HttpStatus.OK, "{\"status\": \"UP\"}"));

        router.register("/hello", request -> new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                com.jhanvi857.coreHTTP.protocol.HttpStatus.OK, "Hello from Router!"));

        new HttpServer(8080).start(router);
    }

    private static String resolveDefaultStaticDir() {
        // Why need ?
        // coz The server may be launched from project root, scripts folder, IDE, or CI
        // runners and a single relative path breaks in some of those cases and causes
        // 404 on '/'.
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

        // fallback for environments where assets are external..will override with
        // -Dcorehttp.staticDir or COREHTTP_STATIC_DIR.
        return cwd.resolve("src/main/resources/public").normalize().toString();
    }

    private void sendServiceUnavailable(Socket socket) {
        try {
            HttpResponse response = new HttpResponse(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "<h1>503 Service Unavailable</h1><p>Server is busy. Please retry shortly.</p>");
            response.writeTo(socket.getOutputStream());
        } catch (IOException ignored) {
            logger.error("Failed to transmit 503 Service Unavailable response: {}", ignored.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
                logger.error("Failed to close socket for rejected connection: {}", ignored.getMessage());
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