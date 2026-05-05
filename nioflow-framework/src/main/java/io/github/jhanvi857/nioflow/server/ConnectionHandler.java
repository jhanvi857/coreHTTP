package io.github.jhanvi857.nioflow.server;

import io.github.jhanvi857.nioflow.protocol.HttpParser;
import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.exception.HttpParseException;

import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    private final SocketChannel channel;
    private final io.github.jhanvi857.nioflow.routing.Router router;
    private final SelectionKey key;
    private final java.io.InputStream inStream;
    private final java.io.OutputStream outStream;
    private final ExecutorService routeExecutor;

    public ConnectionHandler(SocketChannel channel, java.io.InputStream inStream, java.io.OutputStream outStream,
            io.github.jhanvi857.nioflow.routing.Router router, SelectionKey key, ExecutorService routeExecutor) {
        this.channel = channel;
        this.inStream = inStream;
        this.outStream = outStream;
        this.router = router;
        this.key = key;
        this.routeExecutor = routeExecutor;
    }

    @Override
    public void run() {
        try {
            java.io.InputStream in = this.inStream;
            HttpParser parser = new HttpParser();

            boolean keepAlive = true;
            while (keepAlive && channel.isOpen()) {
                HttpRequest request;
                try {
                    request = parser.parse(in);
                    if (channel.getRemoteAddress() != null) {
                        request.setRemoteAddress(channel.getRemoteAddress().toString().replace("/", ""));
                    }
                } catch (HttpParseException e) {
                    logger.warn("Malformed request from client: {}", e.getMessage());
                    sendErrorResponse(io.github.jhanvi857.nioflow.protocol.HttpStatus.BAD_REQUEST, "Bad Request");
                    break;
                } catch (io.github.jhanvi857.nioflow.exception.PayloadTooLargeException e) {
                    logger.warn("Payload too large: {}", e.getMessage());
                    sendErrorResponse(io.github.jhanvi857.nioflow.protocol.HttpStatus.fromCode(413),
                            "Payload Too Large");
                    break;
                } catch (io.github.jhanvi857.nioflow.exception.RequestHeaderFieldsTooLargeException e) {
                    logger.warn("Headers too large: {}", e.getMessage());
                    sendErrorResponse(io.github.jhanvi857.nioflow.protocol.HttpStatus.fromCode(431),
                            "Request Header Fields Too Large");
                    break;
                } catch (SocketTimeoutException e) {
                    break;
                } catch (java.io.IOException e) {
                    break;
                }

                String connectionHeader = request.getHeaders().getOrDefault("Connection", "close");
                keepAlive = connectionHeader.equalsIgnoreCase("keep-alive");

                io.github.jhanvi857.nioflow.routing.HttpContext ctx = router.dispatch(request, routeExecutor);
                io.github.jhanvi857.nioflow.protocol.HttpResponse response = ctx.getResponse();

                if (!keepAlive) {
                    response.addHeader("Connection", "close");
                } else {
                    response.addHeader("Connection", "keep-alive");
                }

                if (!ctx.isDropResponse()) {
                    response.writeTo(this.outStream);
                } else {
                    keepAlive = false;
                }

                if (!keepAlive) {
                    break;
                }

                // Removed in.available() check to allow parser.parse(in) to wait for next request in keep-alive mode
            }
        } catch (Exception e) {
            if (channel.isOpen()) {
                logger.error("Error processing client request: {}", e.getMessage());
            }
        } finally {
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

    private void sendErrorResponse(io.github.jhanvi857.nioflow.protocol.HttpStatus status, String message) {
        try {
            io.github.jhanvi857.nioflow.protocol.HttpResponse response = new io.github.jhanvi857.nioflow.protocol.HttpResponse(
                    status, "<h1>" + status.getCode() + " " + message + "</h1>");
            response.writeTo(this.outStream);
            this.outStream.flush();

            Thread.sleep(10);
        } catch (Exception e) {
            logger.error("Failed to send error: {}", e.getMessage());
        }
    }
}
