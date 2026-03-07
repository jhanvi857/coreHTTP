package com.jhanvi857.coreHTTP.server;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;
import com.jhanvi857.coreHTTP.routing.RouteHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class StaticFileHandler implements RouteHandler {

    private final Path baseDir;

    public StaticFileHandler(String baseDir) {
        // Canonical root path ensures that all requests are constrained inside this directory.
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        String rawPath = request.getPath();
        String decodedPath;

        // Decodeing %xx sequences first so encoded traversal payloads are visible to validation logic.
        try {
            decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return new HttpResponse(HttpStatus.BAD_REQUEST, "Malformed URL path");
        }

        // Ignoring query parameters for static file mapping.
        int queryIdx = decodedPath.indexOf('?');
        if (queryIdx >= 0) {
            decodedPath = decodedPath.substring(0, queryIdx);
        }

        if (decodedPath.isEmpty() || "/".equals(decodedPath)) {
            decodedPath = "/index.html";
        }

        // Converting URL path to a relative filesystem path before resolution
        String relativePath = decodedPath.startsWith("/") ? decodedPath.substring(1) : decodedPath;
        Path requestedFile = baseDir.resolve(relativePath).normalize();

        if (!requestedFile.startsWith(baseDir)) {
            return new HttpResponse(HttpStatus.BAD_REQUEST, "Invalid path");
        }

        if (Files.exists(requestedFile) && !Files.isDirectory(requestedFile)) {
            byte[] fileBytes = Files.readAllBytes(requestedFile);

            HttpResponse response = new HttpResponse(HttpStatus.OK, fileBytes);

            // Using OS/JDK MIME probing first, then predictable extension fallback.
            String mimeType = Files.probeContentType(requestedFile);
            if (mimeType == null) {
                mimeType = detectMimeTypeByExtension(requestedFile);
            }
            response.addHeader("Content-Type", mimeType);

            // Browser safety header- helps prevent MIME confusion or XSS vectors.
            response.addHeader("X-Content-Type-Options", "nosniff");

            return response;
        } else {
            return new HttpResponse(HttpStatus.NOT_FOUND, "<h1>404 File Not Found</h1>");
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
