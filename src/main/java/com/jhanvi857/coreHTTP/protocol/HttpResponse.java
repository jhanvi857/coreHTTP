package com.jhanvi857.coreHTTP.protocol;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final HttpStatus status;
    private final Map<String, String> headers;
    private final String body;

    public HttpResponse(HttpStatus status, String body) {
        this.status = status;
        this.headers = new HashMap<>();
        this.body = body;
        // default headers
        this.headers.put("Content-Type", "text/plain");
        this.headers.put("Content-Length", String.valueOf(body.length()));
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    // Convert response to bytes efficiently
    @Override
    public String toString() {
        StringBuilder response = new StringBuilder();
        // Status Line
        response.append("HTTP/1.1 ")
                .append(status.getCode())
                .append(" ")
                .append(status.getMessage())
                .append("\r\n");

        // Headers calling
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        // Blank line before body
        response.append("\r\n");

        // Body
        if (body != null) {
            response.append(body);
        }

        return response.toString();
    }
}