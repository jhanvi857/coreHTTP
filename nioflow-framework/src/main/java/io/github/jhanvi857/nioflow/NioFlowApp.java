package io.github.jhanvi857.nioflow;

import io.github.jhanvi857.nioflow.db.Database;
import io.github.jhanvi857.nioflow.middleware.Middleware;
import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.replay.RequestReplayFeature;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.Route;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import io.github.jhanvi857.nioflow.routing.RouteRegistration;
import io.github.jhanvi857.nioflow.routing.Router;
import io.github.jhanvi857.nioflow.server.HttpServer;
import java.util.Map;
import io.github.jhanvi857.nioflow.util.HotReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioFlowApp {
    private static final Logger logger = LoggerFactory.getLogger(NioFlowApp.class);
    private final Router router;
    private HttpServer activeServer;
    private RequestReplayFeature replayFeature;

    public NioFlowApp() {
        this.router = new Router();
    }

    /**
     * Enables nodemon-like hot reload. If enabled via NIOFLOW_WATCH=true,
     * this method will block the parent process and manage a child process
     * that restarts on file changes.
     * 
     * @param mainClass The main class of your application
     * @param args      The command line arguments passed to main
     */
    public static void enableHotReload(Class<?> mainClass, String[] args) {
        HotReloader.setup(mainClass, args);
    }

    public RouteRegistration get(String path, RouteHandler handler) {
        Route route = router.get(path, handler);
        return new RouteRegistration(route);
    }

    public RouteRegistration post(String path, RouteHandler handler) {
        Route route = router.post(path, handler);
        return new RouteRegistration(route);
    }

    public RouteRegistration put(String path, RouteHandler handler) {
        Route route = router.put(path, handler);
        return new RouteRegistration(route);
    }

    public RouteRegistration delete(String path, RouteHandler handler) {
        Route route = router.delete(path, handler);
        return new RouteRegistration(route);
    }

    public NioFlowApp use(Middleware middleware) {
        router.use(middleware);
        return this;
    }

    public NioFlowApp group(String prefix,
            java.util.function.Consumer<io.github.jhanvi857.nioflow.routing.RouteGroup> config) {
        io.github.jhanvi857.nioflow.routing.RouteGroup group = new io.github.jhanvi857.nioflow.routing.RouteGroup(
                prefix, router);
        config.accept(group);
        return this;
    }

    public NioFlowApp register(NioFlowPlugin plugin) {
        plugin.onRegister(this);
        return this;
    }

    public <T extends Exception> NioFlowApp exception(Class<T> exceptionClass,
            io.github.jhanvi857.nioflow.exception.ExceptionHandler handler) {
        router.addExceptionHandler(exceptionClass, handler);
        return this;
    }

    public NioFlowApp onError(io.github.jhanvi857.nioflow.exception.ExceptionHandler handler) {
        return exception(Exception.class, handler);
    }

    /**
     * Initializes PostgreSQL with environment variables (JDBC_URL, etc.)
     */
    public NioFlowApp initPostgres() {
        Database.initPostgres();
        return this;
    }

    /**
     * Initializes MongoDB with environment variables (MONGO_URI)
     */
    public NioFlowApp initMongo() {
        Database.initMongo();
        return this;
    }

    public void listen(int port) {
        this.activeServer = new HttpServer(port);
        this.activeServer.start(router);
    }

    public int getPort() {
        return activeServer != null ? activeServer.getPort() : -1;
    }

    public void listenSecure(int port, String keystorePath, String password) {
        try {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("JKS");
            try (java.io.InputStream is = new java.io.FileInputStream(keystorePath)) {
                ks.load(is, password.toCharArray());
            }
            javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory
                    .getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            this.activeServer = new HttpServer(port, sslContext);
            this.activeServer.start(router);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start secure server", e);
        }
    }

    public void drainAndStop(long timeout, java.util.concurrent.TimeUnit unit) {
        if (this.activeServer != null) {
            this.activeServer.drainAndStop(timeout, unit);
        }
        Database.shutdown();
    }

    public HttpContext dispatch(HttpRequest request, java.util.concurrent.ExecutorService routeExecutor) {
        return router.dispatch(request, routeExecutor);
    }

    public NioFlowApp enableReplay(int capacity) {
        if (!Env.getAsBoolean("NIOFLOW_REPLAY_ENABLED", false)) {
            logger.warn("Request replay is disabled. Set NIOFLOW_REPLAY_ENABLED=true to enable.");
            return this;
        }

        this.replayFeature = new RequestReplayFeature(capacity);
        this.use((ctx, next) -> replayFeature.middleware(next).handle(ctx));

        this.get("/_replay", ctx -> ctx.status(HttpStatus.OK).json(replayFeature.dump()));

        this.post("/_replay/:index", ctx -> {
            String rawIndex = ctx.pathParam("index");
            if (rawIndex == null || rawIndex.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Missing replay index"));
                return;
            }

            int index;
            try {
                index = Integer.parseInt(rawIndex);
            } catch (NumberFormatException ex) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Replay index must be numeric"));
                return;
            }

            Map<String, Object> result = replayFeature.replayIndex(router, index, ctx.routeExecutor());
            if (result.containsKey("error")) {
                ctx.status(HttpStatus.NOT_FOUND).json(result);
                return;
            }
            ctx.status(HttpStatus.OK).json(result);
        });

        return this;
    }
}
