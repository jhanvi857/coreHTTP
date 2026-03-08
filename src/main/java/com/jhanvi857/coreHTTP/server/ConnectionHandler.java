package com.jhanvi857.coreHTTP.server;

import com.jhanvi857.coreHTTP.protocol.HttpParser;
import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.exception.HttpParseException;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    private final Socket socket;
    private final com.jhanvi857.coreHTTP.routing.Router router;

    public ConnectionHandler(Socket socket, com.jhanvi857.coreHTTP.routing.Router router) {
        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        logger.debug("Handling client: {}", socket.getRemoteSocketAddress());

        try (InputStream in = socket.getInputStream()) {
            HttpParser parser = new HttpParser();

            // Keep-Alive Loop
            // keep the connection open as long as the client wants to talk and the socket
            // hasn't timed out.
            boolean keepAlive = true;
            while (keepAlive && !socket.isClosed()) {
                HttpRequest request;
                try {
                    request = parser.parse(in);
                } catch (HttpParseException e) {
                    logger.warn("Received a broken request from {}: {}", socket.getRemoteSocketAddress(),
                            e.getMessage());
                    sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus.BAD_REQUEST,
                            "Bad Request: " + e.getMessage());
                    // closing the connection on parse errors
                    break;
                } catch (SocketTimeoutException e) {
                    // if the client stays connected but doesn't send a new request
                    logger.debug("Keep-alive connection timed out for {}", socket.getRemoteSocketAddress());
                    break;
                } catch (java.io.IOException e) {

                    break;
                }

                // Deciding if should keep the connection open based on the connection header
                String connectionHeader = request.getHeaders().getOrDefault("Connection", "close");
                keepAlive = connectionHeader.equalsIgnoreCase("keep-alive");

                // Route the request and get a response
                com.jhanvi857.coreHTTP.routing.RouteHandler handler = router.resolve(request);
                com.jhanvi857.coreHTTP.protocol.HttpResponse response;

                if (handler != null) {
                    response = handler.handle(request);
                } else {
                    response = new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                            com.jhanvi857.coreHTTP.protocol.HttpStatus.NOT_FOUND,
                            "<h1>404 Not Found</h1>");
                }

                // If not keeping the connection, tell the client we are closing it
                if (!keepAlive) {
                    response.addHeader("Connection", "close");
                } else {
                    response.addHeader("Connection", "keep-alive");
                }

                // Send the response back to browser
                response.writeTo(socket.getOutputStream());

                // If it's a one-off request, stop the loop
                if (!keepAlive) {
                    break;
                }
            }
        } catch (Exception e) {
            if (!socket.isClosed()) {
                logger.error("Internal error handling client {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
                sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error");
            }
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus status, String message) {
        try {
            com.jhanvi857.coreHTTP.protocol.HttpResponse response = new com.jhanvi857.coreHTTP.protocol.HttpResponse(
                    status, "<h1>" + status.getCode() + " " + message + "</h1>");
            response.writeTo(socket.getOutputStream());
        } catch (java.io.IOException e) {
            logger.error("Failed to transmit error response ({}) to {}: {}", status, socket.getRemoteSocketAddress(),
                    e.getMessage());
        }
    }
}