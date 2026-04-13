package io.github.jhanvi857.nioflow.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final HttpStatus status;
    private final Map<String, String> headers;
    private byte[] bodyBytes;
    private InputStream bodyStream;
    private long bodyLength = -1;

    public HttpResponse(HttpStatus status, byte[] body) {
        this.status = status;
        this.headers = new HashMap<>();
        this.bodyBytes = body != null ? body : new byte[0];
        this.bodyLength = this.bodyBytes.length;
        // default headers
        this.headers.put("Content-Type", "application/octet-stream");
        this.headers.put("Content-Length", String.valueOf(this.bodyLength));
    }

    public HttpResponse(HttpStatus status, InputStream stream, long length) {
        this.status = status;
        this.headers = new HashMap<>();
        this.bodyStream = stream;
        this.bodyLength = length;
        this.headers.put("Content-Type", "application/octet-stream");
        if (length >= 0) {
            this.headers.put("Content-Length", String.valueOf(length));
        } else {
            this.headers.put("Transfer-Encoding", "chunked");
        }
    }

    public HttpResponse(HttpStatus status, String body) {
        this(status, body != null ? body.getBytes(StandardCharsets.UTF_8) : null);
        this.headers.put("Content-Type", "text/plain"); // Override default for String
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void setContentType(String contentType) {
        this.headers.put("Content-Type", contentType);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getHeadersMap() {
        return headers;
    }

    public byte[] getBody() {
        return bodyBytes;
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
        if (bodyBytes != null && bodyBytes.length > 0) {
            out.write(bodyBytes);
        } else if (bodyStream != null) {
            if (headers.getOrDefault("Transfer-Encoding", "").equals("chunked")) {
                writeChunked(out);
            } else {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = bodyStream.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        }
        out.flush();
    }

    private void writeChunked(OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = bodyStream.read(buffer)) != -1) {
            String sizeHex = Integer.toHexString(read) + "\r\n";
            out.write(sizeHex.getBytes(StandardCharsets.UTF_8));
            out.write(buffer, 0, read);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        out.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "status=" + status +
                ", headers=" + headers +
                '}';
    }
}
