package com.jhanvi857.coreHTTP.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final HttpStatus status;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpResponse(HttpStatus status, byte[] body) {
        this.status = status;
        this.headers = new HashMap<>();
        this.body = body != null ? body : new byte[0];
        // default headers
        this.headers.put("Content-Type", "application/octet-stream");
        this.headers.put("Content-Length", String.valueOf(this.body.length));
    }

    public HttpResponse(HttpStatus status, String body) {
        this(status, body != null ? body.getBytes(StandardCharsets.UTF_8) : null);
        this.headers.put("Content-Type", "text/plain"); // Override default for String
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void writeTo(OutputStream out) throws IOException {
        StringBuilder response = new StringBuilder();

        // Status Line
        response.append("HTTP/1.1 ")
                .append(status.getCode())
                .append(" ")
                .append(status.getMessage())
                .append("\r\n");

        // Headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        // Blank line before body
        response.append("\r\n");

        // Write headers
        out.write(response.toString().getBytes(StandardCharsets.UTF_8));

        // Write Body
        if (body.length > 0) {
            out.write(body);
        }
        out.flush();
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "status=" + status +
                ", headers=" + headers +
                '}';
    }
}