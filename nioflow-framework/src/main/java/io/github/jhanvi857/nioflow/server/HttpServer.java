package io.github.jhanvi857.nioflow.server;

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

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private final int port;
    private final ThreadPoolExecutor threadPool;
    private final int socketReadTimeoutMs;
    private volatile boolean running = false;
    private final javax.net.ssl.SSLContext sslContext;

    public HttpServer(int port) {
        this(port, null);
    }

    public HttpServer(int port, javax.net.ssl.SSLContext sslContext) {
        this.port = port;
        this.sslContext = sslContext;
        int workerThreads = readIntSetting("nioflow.threads", "NIOFLOW_THREADS", 64, 1);
        int queueCapacity = readIntSetting("nioflow.queueCapacity", "NIOFLOW_QUEUE_CAPACITY", 1000, 1);
        this.socketReadTimeoutMs = readIntSetting("nioflow.socketTimeoutMs", "NIOFLOW_SOCKET_TIMEOUT_MS", 30000,
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

    public void start(io.github.jhanvi857.nioflow.routing.Router router) {
        this.running = true;

        // Removed internal shutdown hook. User should manage application lifecycle via drainAndStop()

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

            logger.info("NioFlow NIO server listening on port {}", port);

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
            this.running = false;
            logger.error("Critical NIO failure: {}", e.getMessage(), e);
            throw new RuntimeException("Server failed to start", e);
        }
    }

    private void acceptNewClient(ServerSocketChannel serverChannel, io.github.jhanvi857.nioflow.routing.Router router)
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
            java.io.InputStream inStream;
            java.io.OutputStream outStream;

            if (this.sslContext != null) {
                javax.net.ssl.SSLSocketFactory factory = this.sslContext.getSocketFactory();
                java.net.Socket secureSocket = factory.createSocket(clientChannel.socket(), 
                    clientChannel.socket().getInetAddress().getHostAddress(), 
                    clientChannel.socket().getPort(), true);
                ((javax.net.ssl.SSLSocket) secureSocket).setUseClientMode(false);
                ((javax.net.ssl.SSLSocket) secureSocket).startHandshake();
                
                inStream = secureSocket.getInputStream();
                outStream = secureSocket.getOutputStream();
            } else {
                inStream = java.nio.channels.Channels.newInputStream(clientChannel);
                outStream = java.nio.channels.Channels.newOutputStream(clientChannel);
            }

            threadPool.execute(new ConnectionHandler(clientChannel, inStream, outStream, router, null, threadPool));
        } catch (RejectedExecutionException rejected) {
            logger.warn("Server busy! Rejecting connection.");
            sendServiceUnavailable(clientChannel);
        } catch (javax.net.ssl.SSLHandshakeException sslEx) {
            logger.warn("TLS Handshake failure: {}", sslEx.getMessage());
            clientChannel.close();
        }
    }

    @SuppressWarnings("unused")
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
        // -Dnioflow.staticDir or NIOFLOW_STATIC_DIR.
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

    public void drainAndStop(long timeout, TimeUnit unit) {
        logger.info("Shutdown signal received. Starting graceful shutdown...");
        this.running = false;
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(timeout, unit)) {
                threadPool.shutdownNow();
            }
            logger.info("NioFlow server stopped successfully.");
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
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
