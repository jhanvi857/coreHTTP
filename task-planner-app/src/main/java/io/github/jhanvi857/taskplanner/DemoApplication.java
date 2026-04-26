package io.github.jhanvi857.taskplanner;

import io.github.jhanvi857.nioflow.Env;
import io.github.jhanvi857.nioflow.NioFlowApp;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.taskplanner.controller.TaskController;
import io.github.jhanvi857.taskplanner.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        NioFlowApp.enableHotReload(DemoApplication.class, args);
        
        boolean authDisabled = Env.getAsBoolean("NIOFLOW_DISABLE_AUTH", false);
        String jwtSecret = Env.get("JWT_SECRET", Env.get("nioflow.jwtSecret"));
        
        if (!authDisabled && (jwtSecret == null || jwtSecret.length() < 32)) {
            logger.error("JWT_SECRET must be set (min 32 characters). "
                     + "Set the JWT_SECRET in your .env file or environment variable.");
            System.exit(1);
        }

        if (authDisabled) {
            logger.warn("NIOFLOW_DISABLE_AUTH=true. Protected routes are running without JWT checks.");
        }

        NioFlowApp app = new NioFlowApp();
        app.initPostgres();
        if (Env.get("MONGO_URI") != null) {
            app.initMongo();
        }
        
        boolean exposeErrorDetails = Env.getAsBoolean("NIOFLOW_EXPOSE_ERROR_DETAILS", false);

        // 1. Global Middleware
        app.use(new io.github.jhanvi857.nioflow.middleware.LoggerMiddleware());
        // configurable cors origin. Default to localhost for dev safety.
        String corsOrigin = Env.get("NIOFLOW_CORS_ORIGIN", "http://localhost:3000");
        app.use(new io.github.jhanvi857.nioflow.middleware.CorsMiddleware(corsOrigin));
        app.use(new io.github.jhanvi857.nioflow.middleware.MetricsMiddleware());
        // Simple Rate Limit: 100,000 requests per 10 seconds for load testing
        app.use(new io.github.jhanvi857.nioflow.middleware.RateLimitMiddleware(100000, 10000));

        // 2. Resolving static assets
        String staticDir = Env.get("nioflow.staticDir", Env.get("NIOFLOW_STATIC_DIR", resolveDefaultStaticDir()));
        logger.info("Serving static assets from: {}", staticDir);

        // 4. Observability Routes
        app.register(new io.github.jhanvi857.nioflow.plugin.HealthCheckPlugin());
        app.get("/_ready", ctx -> {
            boolean dbEnabled = DatabaseManager.isEnabled();
            boolean dbHealthy = DatabaseManager.isHealthy();

            if (!dbEnabled || dbHealthy) {
                ctx.status(HttpStatus.OK).json(java.util.Map.of(
                        "status", "UP",
                        "database", dbEnabled ? "UP" : "DISABLED"));
                return;
            }

            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(java.util.Map.of(
                    "status", "DOWN",
                    "database", "DOWN"));
        });

        app.get("/metrics", ctx -> {
            ctx.status(HttpStatus.OK).send(io.github.jhanvi857.nioflow.middleware.MetricsMiddleware.getMetricsReport());
        });

        app.group("/api/tasks", tasks -> {
            if (!authDisabled) {
                tasks.use(new io.github.jhanvi857.nioflow.middleware.AuthMiddleware());
            }
            TaskController taskController = new TaskController();
            tasks.get("/", taskController::list);
            tasks.post("/", taskController::create);
            tasks.get("/:id", taskController::get);
            tasks.delete("/:id", taskController::delete);
        });

        app.group("/api/secure", secure -> {
            if (!authDisabled) {
                secure.use(new io.github.jhanvi857.nioflow.middleware.AuthMiddleware());
            }
            secure.get("/", ctx -> {
                String user = ctx.header("X-Auth-User");
                if (user == null || user.isBlank()) {
                    user = "anonymous";
                }
                ctx.status(HttpStatus.OK).json(java.util.Map.of("message", "Hello, " + user));
            });
        });

        // 7. Global Custom Exception Handlers
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            if (exposeErrorDetails) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(java.util.Map.of("error", "Bad Request", "details", e.getMessage()));
            } else {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(java.util.Map.of("error", "Bad Request"));
            }
        });
        
        app.onError((err, ctx) -> {
            logger.error("Unhandled Exception: ", err);
            if (exposeErrorDetails) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(java.util.Map.of("error", "Internal Server Error", "details", err.getMessage()));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(java.util.Map.of("error", "Internal Server Error"));
            }
        });

        // 8. Static File Routes
        app.register(new io.github.jhanvi857.nioflow.plugin.StaticFilesPlugin(staticDir, "/"));

        // Register Graceful Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.drainAndStop(30, java.util.concurrent.TimeUnit.SECONDS);
        }));

        boolean tlsEnabled = Env.getAsBoolean("NIOFLOW_TLS_ENABLED", false);
        if (tlsEnabled) {
            String keystorePath = Env.get("NIOFLOW_TLS_KEYSTORE_PATH");
            String keystorePassword = Env.get("NIOFLOW_TLS_KEYSTORE_PASSWORD");

            if (keystorePath == null || keystorePath.isBlank() || keystorePassword == null || keystorePassword.isBlank()) {
                throw new IllegalStateException(
                        "NIOFLOW_TLS_ENABLED=true requires NIOFLOW_TLS_KEYSTORE_PATH and NIOFLOW_TLS_KEYSTORE_PASSWORD.");
            }

            int tlsPort = Env.getAsInt("NIOFLOW_TLS_PORT", 8443);
            logger.info("Starting TLS listener on port {} using keystore {}", tlsPort, keystorePath);
            app.listenSecure(tlsPort, keystorePath, keystorePassword);
            return;
        }

        app.listen(Env.getAsInt("PORT", 8080));
    }

    private static String resolveDefaultStaticDir() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path[] candidates = new Path[] {
                cwd.resolve("src/main/resources/public").normalize(),
                cwd.resolve("task-planner-app/src/main/resources/public").normalize(),
                cwd.resolve("../src/main/resources/public").normalize(),
                cwd.resolve("target/public").normalize(),
                cwd.resolve("task-planner-app/target/public").normalize()
        };

        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate.toString();
            }
        }

        return cwd.resolve("src/main/resources/public").normalize().toString();
    }
}
