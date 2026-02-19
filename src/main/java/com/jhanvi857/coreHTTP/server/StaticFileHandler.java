package com.jhanvi857.coreHTTP.server;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;
import com.jhanvi857.coreHTTP.routing.RouteHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StaticFileHandler implements RouteHandler {

    private final String baseDir;

    public StaticFileHandler(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        String path = request.getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        // Preventing directory traversal attack with simple check
        if (path.contains("..")) {
            return new HttpResponse(HttpStatus.BAD_REQUEST, "Invalid path");
        }

        File file = new File(baseDir + path);

        if (file.exists() && !file.isDirectory()) {
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            HttpResponse response = new HttpResponse(HttpStatus.OK, fileBytes);

            // MIME type detection
            if (path.endsWith(".html")) {
                response.addHeader("Content-Type", "text/html");
            } else if (path.endsWith(".css")) {
                response.addHeader("Content-Type", "text/css");
            } else if (path.endsWith(".js")) {
                response.addHeader("Content-Type", "application/javascript");
            } else if (path.endsWith(".png")) {
                response.addHeader("Content-Type", "image/png");
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                response.addHeader("Content-Type", "image/jpeg");
            } else if (path.endsWith(".gif")) {
                response.addHeader("Content-Type", "image/gif");
            } else if (path.endsWith(".json")) {
                response.addHeader("Content-Type", "application/json");
            }

            return response;
        } else {
            return new HttpResponse(HttpStatus.NOT_FOUND, "<h1>404 File Not Found</h1>");
        }
    }
}
