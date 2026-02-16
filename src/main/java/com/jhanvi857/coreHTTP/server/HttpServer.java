package com.jhanvi857.coreHTTP.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class HttpServer {
    private final int port;
    public HttpServer(int port) {
        this.port = port;
    }
    public void start() {
        System.out.println("Starting TCP server on port : "+port);
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Waiting for the client...");
                Socket clienSocket = serverSocket.accept();
                System.out.println("Client accepted.");
                ConnectionHandler handler = new ConnectionHandler(clienSocket);
                new Thread(handler).start();
            }
        } catch(IOException e) {
            System.out.println("Server failed ! "+e.getMessage());
        }
    }

    public void handleClient(Socket clientSocket) {
        try {
            System.out.println("Client connected : "+clientSocket.getRemoteSocketAddress());
            System.out.println("Handled by thread : "+Thread.currentThread().getName());
            Thread.sleep(60_000);
        } catch(Exception e) {
            System.out.println("Error on connecting client in handleClient. "+e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch(Exception e) {

            }
        }
    }
    public static void main(String[] args) {
        new HttpServer(8080).start();
    }
}