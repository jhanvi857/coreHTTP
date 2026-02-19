package com.jhanvi857.coreHTTP.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

        // static file handler for the path
        router.register("/", new StaticFileHandler("src/main/resources/public"));
        router.register("/hello", request -> new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                com.jhanvi857.coreHTTP.protocol.HttpStatus.OK, "Hello from Router!"));

        new HttpServer(8080).start(router);
    }
}