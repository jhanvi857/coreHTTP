package com.jhanvi857.coreHTTP.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
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

        // Shutdown Hook
        // Ensure to clean up and finish current requests when stopping.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received. Starting graceful shutdown...");
            this.running = false;
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
                logger.info("CoreHTTP server stopped successfully.");
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));

        try {
            // 1. Open the selector to check whether current client is sending more request
            // or have just occupied thread.
            Selector selector = Selector.open();

            // 2. Open the server channel to accept new client connections.
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));

            // set non-blocking mode
            serverChannel.configureBlocking(false);

            // 3. Tell the selector we want to know when someone accepts our door
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("CoreHTTP NIO server listening on port {}", port);

            // nio main loop.
            while (running) {
                // Wait for an event
                if (selector.select(1000) == 0) {
                    continue;
                }

                // Look at all the events that occurred
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    // Removing so don't process it twice
                    keys.remove();

                    if (!key.isValid())
                        continue;

                    if (key.isAcceptable()) {
                        // Accept and hand off directly to a worker thread.
                        acceptNewClient(serverChannel, router);
                    }
                }
            }

            selector.close();
            serverChannel.close();

        } catch (IOException e) {
            if (running) {
                logger.error("Critical NIO failure: {}", e.getMessage(), e);
            }
        }
    }

    private void acceptNewClient(ServerSocketChannel serverChannel, com.jhanvi857.coreHTTP.routing.Router router)
            throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) {
            return;
        }

        // Worker threads parse request bytes using InputStream semantics.
        // Keep accepted channels in blocking mode to prevent
        // IllegalBlockingModeException.
        clientChannel.configureBlocking(true);
        clientChannel.socket().setSoTimeout(socketReadTimeoutMs);

        logger.info("Accepted connection from {}", clientChannel.getRemoteAddress());

        try {
            threadPool.execute(new ConnectionHandler(clientChannel, router, null));
        } catch (RejectedExecutionException rejected) {
            logger.warn("Server busy! Rejecting connection.");
            sendServiceUnavailable(clientChannel);
        }
    }

    public static void main(String[] args) {
        com.jhanvi857.coreHTTP.routing.Router router = new com.jhanvi857.coreHTTP.routing.Router();

        // 1. Global Middleware
        router.use(new com.jhanvi857.coreHTTP.middleware.LoggerMiddleware());
        // configurable cors origin. Default to localhost for dev safety.
        String corsOrigin = System.getenv("COREHTTP_CORS_ORIGIN");
        if (corsOrigin == null || corsOrigin.isBlank()) {
            corsOrigin = "http://localhost:3000";
        }
        router.use(new com.jhanvi857.coreHTTP.middleware.CorsMiddleware(corsOrigin));
        router.use(new com.jhanvi857.coreHTTP.middleware.MetricsMiddleware());
        // Simple Rate Limit: 100 requests per 10 seconds locally
        router.use(new com.jhanvi857.coreHTTP.middleware.RateLimitMiddleware(100, 10000));

        // 2. Resolving static assets
        String staticDir = System.getProperty("corehttp.staticDir");
        if (staticDir == null || staticDir.isBlank()) {
            staticDir = System.getenv("COREHTTP_STATIC_DIR");
        }
        if (staticDir == null || staticDir.isBlank()) {
            staticDir = resolveDefaultStaticDir();
        }
        logger.info("Serving static assets from: {}", staticDir);

        // 3. Static File Routes
        router.get("/", new StaticFileHandler(staticDir));

        // 4. Observability Routes
        router.get("/_health", new com.jhanvi857.coreHTTP.observability.HealthCheckHandler());
        router.get("/metrics", request -> new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                com.jhanvi857.coreHTTP.protocol.HttpStatus.OK,
                com.jhanvi857.coreHTTP.middleware.MetricsMiddleware.getMetricsReport()));

        // 5. App Business Routes
        com.jhanvi857.coreHTTP.app.controller.TaskController taskController = new com.jhanvi857.coreHTTP.app.controller.TaskController();
        router.get("/api/tasks", taskController::list);
        router.post("/api/tasks", taskController::create);
        router.get("/api/tasks/", taskController::get); // Longest prefix will match /api/tasks/{id}
        router.delete("/api/tasks/", taskController::delete);

        // 6. Auth Demo Endpoint
        router.get("/api/secure", req -> {
            String user = req.getHeaders().getOrDefault("X-Auth-User", "anonymous");
            String safeBody = com.jhanvi857.coreHTTP.util.JsonUtils
                    .toJson(java.util.Map.of("message", "Hello, " + user));
            HttpResponse resp = new HttpResponse(HttpStatus.OK, safeBody);
            resp.addHeader("Content-Type", "application/json");
            return resp;
        });

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

    private void sendServiceUnavailable(SocketChannel channel) {
        try {
            HttpResponse response = new HttpResponse(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "<h1>503 Service Unavailable</h1><p>Server is busy. Please retry shortly.</p>");

            // Simple blocking write for rejection
            response.writeTo(java.nio.channels.Channels.newOutputStream(channel));
        } catch (IOException ignored) {
        } finally {
            try {
                channel.close();
            } catch (IOException ignored) {
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