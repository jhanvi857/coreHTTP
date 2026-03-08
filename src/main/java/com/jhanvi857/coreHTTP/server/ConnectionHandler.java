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

        InputStream in = null;
        try {
            in = socket.getInputStream();

            // phase 3 entry point
            HttpParser parser = new HttpParser();
            HttpRequest request = parser.parse(in);

            // debugging with structured logs
            logger.info("{} {} protocol={}", request.getMethod(), request.getPath(), request.getVersion());
            logger.debug("Headers: {}", request.getHeaders());

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

            response.writeTo(socket.getOutputStream());

        } catch (HttpParseException e) {
            logger.warn("Received malformed HTTP request from {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
            sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus.BAD_REQUEST, "Bad Request: " + e.getMessage());

        } catch (SocketTimeoutException e) {
            // Timeout is expected when a client opens a connection but sends data too
            // slowly.
            // We return 408 instead of 500 so clients know the request timed out.
            logger.warn("Request timed out for client {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
            sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus.REQUEST_TIMEOUT,
                    "Request Timeout");

        } catch (Exception e) {
            logger.error("Internal processing error for client {}: {}", socket.getRemoteSocketAddress(), e.getMessage(),
                    e);
            // sending 500 only when the socket is still open and not already closed
            if (!socket.isClosed()) {
                sendErrorResponse(com.jhanvi857.coreHTTP.protocol.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error");
            }

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignored) {
                logger.error("Failed to clean up client connection for {}: {}", socket.getRemoteSocketAddress(),
                        ignored.getMessage());
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