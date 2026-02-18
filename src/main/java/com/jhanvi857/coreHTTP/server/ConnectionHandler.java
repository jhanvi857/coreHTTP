package com.jhanvi857.coreHTTP.server;

import com.jhanvi857.coreHTTP.protocol.HttpParser;
import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.exception.HttpParseException;

import java.io.InputStream;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private final com.jhanvi857.coreHTTP.routing.Router router;

    public ConnectionHandler(Socket socket, com.jhanvi857.coreHTTP.routing.Router router) {
        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        System.out.println("Handling client: " + socket.getRemoteSocketAddress());

        try (InputStream in = socket.getInputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead = in.read(buffer);

            if (bytesRead == -1) {
                return;
            }

            byte[] requestBytes = new byte[bytesRead];
            System.arraycopy(buffer, 0, requestBytes, 0, bytesRead);

            // phase 3 entry point
            HttpParser parser = new HttpParser();
            HttpRequest request = parser.parse(requestBytes);

            // debugging.
            System.out.println("METHOD  : " + request.getMethod());
            System.out.println("PATH    : " + request.getPath());
            System.out.println("VERSION : " + request.getVersion());
            System.out.println("HEADERS : " + request.getHeaders());

            // Phase 5 & 8: Routing and Response
            com.jhanvi857.coreHTTP.routing.RouteHandler handler = router.resolve(request);
            com.jhanvi857.coreHTTP.protocol.HttpResponse response;

            if (handler != null) {
                response = handler.handle(request);
            } else {
                response = new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                        com.jhanvi857.coreHTTP.protocol.HttpStatus.NOT_FOUND,
                        "<h1>404 Not Found</h1>");
            }

            socket.getOutputStream().write(response.toString().getBytes());
            socket.getOutputStream().flush();

        } catch (HttpParseException e) {
            System.out.println("Bad HTTP request: " + e.getMessage());
            sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus.BAD_REQUEST, "Bad Request: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            // sending 500 only when the socket is still open and not already closed
            if (!socket.isClosed()) {
                sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error");
            }

        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {
                System.out.println("Error in handling client from connection handler" + ignored.getMessage());
            }
        }
    }

    private void sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus status, String message) {
        try {
            com.jhanvi857.coreHTTP.protocol.HttpResponse response = new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                    status, "<h1>" + status.getCode() + " " + message + "</h1>");
            socket.getOutputStream().write(response.toString().getBytes());
            socket.getOutputStream().flush();
        } catch (java.io.IOException e) {
            System.out.println("Failed to send error response: " + e.getMessage());
        }
    }
}