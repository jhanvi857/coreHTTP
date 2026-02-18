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
            String content = new String(fileBytes); // Simpler for now, assuming text files

            HttpResponse response = new HttpResponse(HttpStatus.OK, content);

            // MIME type detection
            if (path.endsWith(".html")) {
                response.addHeader("Content-Type", "text/html");
            } else if (path.endsWith(".css")) {
                response.addHeader("Content-Type", "text/css");
            } else if (path.endsWith(".js")) {
                response.addHeader("Content-Type", "application/javascript");
            } else {
                response.addHeader("Content-Type", "text/plain");
            }

            return response;
        } else {
            return new HttpResponse(HttpStatus.NOT_FOUND, "<h1>404 File Not Found</h1>");
        }
    }
}
