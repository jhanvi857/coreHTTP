package com.jhanvi857.coreHTTP.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private final ExecutorService threadPool;

    public HttpServer(int port) {
        this.port = port;
        // creating pool of 10 threads
        this.threadPool = Executors.newFixedThreadPool(10);
    }

    public void start(com.jhanvi857.coreHTTP.routing.Router router) {
        System.out.println("Starting TCP server on port : " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Waiting for the client...");
                Socket clienSocket = serverSocket.accept();
                System.out.println("Client accepted.");

                ConnectionHandler handler = new ConnectionHandler(clienSocket, router);

                // Submiting the task to the pool instead of creating a new Thread
                threadPool.submit(handler);
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
}