package io.github.jhanvi857.nioflow.server;

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticFileHandler implements RouteHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);
    private final Path baseDir;

    public StaticFileHandler(String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpContext ctx) throws Exception {
        String rawPath = ctx.path();
        String decodedPath;

        try {
            decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            ctx.status(HttpStatus.BAD_REQUEST).send("Malformed URL path");
            return;
        }

        int queryIdx = decodedPath.indexOf('?');
        if (queryIdx >= 0) {
            decodedPath = decodedPath.substring(0, queryIdx);
        }

        if (decodedPath.isEmpty() || "/".equals(decodedPath)) {
            decodedPath = "/index.html";
        }

        String relativePath = decodedPath.startsWith("/") ? decodedPath.substring(1) : decodedPath;
        Path requestedFile = baseDir.resolve(relativePath).normalize();

        if (!requestedFile.startsWith(baseDir)) {
            logger.warn("Path traversal attempt detected: {} (normalized to {})", rawPath, requestedFile);
            ctx.status(HttpStatus.BAD_REQUEST).send("Invalid path");
            return;
        }

        if (Files.exists(requestedFile) && !Files.isDirectory(requestedFile)) {
            logger.debug("Serving file: {}", requestedFile);

            long fileSize = Files.size(requestedFile);
            String mimeType = Files.probeContentType(requestedFile);
            if (mimeType == null) {
                mimeType = detectMimeTypeByExtension(requestedFile);
            }

            HttpResponse response = new FileHttpResponse(HttpStatus.OK, requestedFile, fileSize);
            ctx.setResponse(response);
            ctx.header("Content-Type", mimeType);
            ctx.header("Content-Length", String.valueOf(fileSize));
            ctx.header("X-Content-Type-Options", "nosniff");
        } else {
            logger.info("Static file not found: {}", requestedFile);
            ctx.status(HttpStatus.NOT_FOUND).send("<h1>404 File Not Found</h1>");
        }
    }

    private String detectMimeTypeByExtension(Path requestedFile) {
        String fileName = requestedFile.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (fileName.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (fileName.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (fileName.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }
}
