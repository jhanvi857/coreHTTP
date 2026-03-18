package com.jhanvi857.taskplanner;

import com.jhanvi857.nioflow.NioFlowApp;
import com.jhanvi857.nioflow.protocol.HttpStatus;
import com.jhanvi857.taskplanner.controller.TaskController;
import com.jhanvi857.taskplanner.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        String jwtSecret = System.getProperty("nioflow.jwtSecret");
        if (jwtSecret == null || jwtSecret.isBlank()) {
            jwtSecret = System.getenv("JWT_SECRET");
        }
        if (jwtSecret == null || jwtSecret.length() < 32) {
            LoggerFactory.getLogger(DemoApplication.class)
                .error("JWT_SECRET must be set (min 32 characters). "
                     + "Set the JWT_SECRET environment variable and restart.");
            System.exit(1);
        }

        NioFlowApp app = new NioFlowApp();
        boolean exposeErrorDetails = isTrue(System.getenv("NIOFLOW_EXPOSE_ERROR_DETAILS"));

        // 1. Global Middleware
        app.use(new com.jhanvi857.nioflow.middleware.LoggerMiddleware());
        // configurable cors origin. Default to localhost for dev safety.
        String corsOrigin = System.getenv("NIOFLOW_CORS_ORIGIN");
        if (corsOrigin == null || corsOrigin.isBlank()) {
            corsOrigin = "http://localhost:3000";
        }
        app.use(new com.jhanvi857.nioflow.middleware.CorsMiddleware(corsOrigin));
        app.use(new com.jhanvi857.nioflow.middleware.MetricsMiddleware());
        // Simple Rate Limit: 100 requests per 10 seconds locally
        app.use(new com.jhanvi857.nioflow.middleware.RateLimitMiddleware(100, 10000));

        // 2. Resolving static assets
        String staticDir = System.getProperty("nioflow.staticDir");
        if (staticDir == null || staticDir.isBlank()) {
            staticDir = System.getenv("NIOFLOW_STATIC_DIR");
        }
        if (staticDir == null || staticDir.isBlank()) {
            staticDir = resolveDefaultStaticDir();
        }
        logger.info("Serving static assets from: {}", staticDir);

        // 4. Observability Routes
        app.register(new com.jhanvi857.nioflow.plugin.HealthCheckPlugin());
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
            ctx.status(HttpStatus.OK).send(com.jhanvi857.nioflow.middleware.MetricsMiddleware.getMetricsReport());
        });

        app.group("/api/tasks", tasks -> {
            tasks.use(new com.jhanvi857.nioflow.middleware.AuthMiddleware());
            TaskController taskController = new TaskController();
            tasks.get("/", taskController::list);
            tasks.post("/", taskController::create);
            tasks.get("/:id", taskController::get);
            tasks.delete("/:id", taskController::delete);
        });

        app.group("/api/secure", secure -> {
            secure.use(new com.jhanvi857.nioflow.middleware.AuthMiddleware());
            secure.get("/", ctx -> {
                String user = ctx.header("X-Auth-User");
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
        app.register(new com.jhanvi857.nioflow.plugin.StaticFilesPlugin(staticDir, "/"));

        // Register Graceful Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.drainAndStop(30, java.util.concurrent.TimeUnit.SECONDS);
        }));

        String tlsEnabled = System.getenv("NIOFLOW_TLS_ENABLED");
        if (isTrue(tlsEnabled)) {
            String keystorePath = System.getenv("NIOFLOW_TLS_KEYSTORE_PATH");
            String keystorePassword = System.getenv("NIOFLOW_TLS_KEYSTORE_PASSWORD");

            if (keystorePath == null || keystorePath.isBlank() || keystorePassword == null || keystorePassword.isBlank()) {
                throw new IllegalStateException(
                        "NIOFLOW_TLS_ENABLED=true requires NIOFLOW_TLS_KEYSTORE_PATH and NIOFLOW_TLS_KEYSTORE_PASSWORD.");
            }

            int tlsPort = readPort("NIOFLOW_TLS_PORT", 8443);
            logger.info("Starting TLS listener on port {} using keystore {}", tlsPort, keystorePath);
            app.listenSecure(tlsPort, keystorePath, keystorePassword);
            return;
        }

        app.listen(readPort("PORT", 8080));
    }

    private static boolean isTrue(String value) {
        return value != null && "true".equalsIgnoreCase(value.trim());
    }

    private static int readPort(String envKey, int defaultPort) {
        String value = System.getenv(envKey);
        if (value == null || value.isBlank()) {
            return defaultPort;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return defaultPort;
        }
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
