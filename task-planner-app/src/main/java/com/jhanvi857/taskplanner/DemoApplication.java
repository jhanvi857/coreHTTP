package com.jhanvi857.taskplanner;

import com.jhanvi857.nioflow.NioFlowApp;
import com.jhanvi857.nioflow.protocol.HttpStatus;
import com.jhanvi857.taskplanner.controller.TaskController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        NioFlowApp app = new NioFlowApp();

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
        app.get("/metrics", ctx -> {
            ctx.status(HttpStatus.OK).send(com.jhanvi857.nioflow.middleware.MetricsMiddleware.getMetricsReport());
        });

        app.group("/api/tasks", tasks -> {
            TaskController taskController = new TaskController();
            tasks.get("/", taskController::list);
            tasks.post("/", taskController::create);
            tasks.get("/:id", taskController::get);
            tasks.delete("/:id", taskController::delete);
        });

        // 6. Auth Demo Endpoint
        app.get("/api/secure", ctx -> {
            String user = ctx.header("X-Auth-User");
            if (user == null) {
                user = "anonymous";
            }
            ctx.status(HttpStatus.OK).json(java.util.Map.of("message", "Hello, " + user));
        });

        // 7. Global Custom Exception Handlers
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(java.util.Map.of("error", "Bad Request", "details", e.getMessage()));
        });

        // 8. Static File Routes
        app.register(new com.jhanvi857.nioflow.plugin.StaticFilesPlugin(staticDir, "/"));

        app.listen(8080);
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
