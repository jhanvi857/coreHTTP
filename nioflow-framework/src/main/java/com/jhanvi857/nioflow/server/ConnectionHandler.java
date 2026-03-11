package com.jhanvi857.nioflow.server;

import com.jhanvi857.nioflow.protocol.HttpParser;
import com.jhanvi857.nioflow.protocol.HttpRequest;
import com.jhanvi857.nioflow.exception.HttpParseException;

// import java.io.InputStream;
import java.net.SocketTimeoutException;
// import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    private final SocketChannel channel;
    private final com.jhanvi857.nioflow.routing.Router router;
    private final SelectionKey key;

    public ConnectionHandler(SocketChannel channel, com.jhanvi857.nioflow.routing.Router router, SelectionKey key) {
        this.channel = channel;
        this.router = router;
        this.key = key;
    }

    @Override
    public void run() {
        try {
            // using java.nio.channels.Channels to bridge the NIO channel to a standard
            // stream
            java.io.InputStream in = java.nio.channels.Channels.newInputStream(channel);
            HttpParser parser = new HttpParser();

            // handle multiple requests on the same connection.
            boolean keepAlive = true;
            while (keepAlive && channel.isOpen()) {
                HttpRequest request;
                try {
                    request = parser.parse(in);
                } catch (HttpParseException e) {
                    logger.warn("Malformed request from client: {}", e.getMessage());
                    sendErrorResponse(com.jhanvi857.nioflow.protocol.HttpStatus.BAD_REQUEST, "Bad Request");
                    break;
                } catch (SocketTimeoutException e) {
                    // Connection idle for too long
                    break;
                } catch (java.io.IOException e) {
                    // Client closed connection
                    break;
                }

                String connectionHeader = request.getHeaders().getOrDefault("Connection", "close");
                keepAlive = connectionHeader.equalsIgnoreCase("keep-alive");

                com.jhanvi857.nioflow.routing.Route route = router.resolve(request.getMethod(), request.getPath());
                com.jhanvi857.nioflow.protocol.HttpResponse response;
                com.jhanvi857.nioflow.routing.HttpContext ctx = new com.jhanvi857.nioflow.routing.HttpContext(
                        request);

                if (route != null) {
                    try {
                        route.extractPathParams(request.getPath()).forEach(ctx::addPathParam);
                        route.getHandler().handle(ctx);
                        response = ctx.getResponse();
                    } catch (Exception e) {
                        com.jhanvi857.nioflow.exception.ExceptionHandler handler = router
                                .getExceptionHandler(e.getClass());
                        if (handler != null) {
                            handler.handle(e, ctx);
                            response = ctx.getResponse();
                        } else {
                            response = com.jhanvi857.nioflow.exception.GlobalExceptionHandler.handle(e);
                            ctx.setResponse(response);
                        }
                    }
                } else {
                    response = new com.jhanvi857.nioflow.protocol.HttpResponse(
                            com.jhanvi857.nioflow.protocol.HttpStatus.NOT_FOUND,
                            "<h1>404 Not Found</h1>");
                    ctx.setResponse(response);
                }

                if (!keepAlive) {
                    response.addHeader("Connection", "close");
                } else {
                    response.addHeader("Connection", "keep-alive");
                }

                // Send the response
                response.writeTo(java.nio.channels.Channels.newOutputStream(channel));

                if (!keepAlive) {
                    break;
                }

                // If the stream is empty for now, stop the thread and wait for Selector
                if (in.available() == 0) {
                    break;
                }
            }
        } catch (Exception e) {
            if (channel.isOpen()) {
                logger.error("Error processing client request: {}", e.getMessage());
            }
        } finally {
            // THE NIO HANDOFF:
            // If want to keep the connection, register it back with the Selector for READ
            // events.
            // If not,close it.
            if (key != null && key.isValid() && channel.isOpen()) {
                key.interestOps(SelectionKey.OP_READ);
                key.selector().wakeup();
            } else {
                try {
                    channel.close();
                } catch (java.io.IOException ignored) {
                }
            }
        }
    }

    private void sendErrorResponse(com.jhanvi857.nioflow.protocol.HttpStatus status, String message) {
        try {
            com.jhanvi857.nioflow.protocol.HttpResponse response = new com.jhanvi857.nioflow.protocol.HttpResponse(
                    status, "<h1>" + status.getCode() + " " + message + "</h1>");
            response.writeTo(java.nio.channels.Channels.newOutputStream(channel));
        } catch (java.io.IOException e) {
            logger.error("Failed to send error: {}", e.getMessage());
        }
    }
}