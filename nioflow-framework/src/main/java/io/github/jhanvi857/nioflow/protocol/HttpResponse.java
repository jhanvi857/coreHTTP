package io.github.jhanvi857.nioflow.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private HttpStatus status;
    private Map<String, String> headers;
    private byte[] bodyBytes;
    private InputStream bodyStream;
    private long bodyLength = -1;

    public HttpResponse(HttpStatus status, byte[] body) {
        this.status = status;
        this.headers = new HashMap<>();
        this.bodyBytes = body != null ? body : new byte[0];
        this.bodyLength = this.bodyBytes.length;
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
        this.headers.put("Content-Type", "text/plain");
    }

    public HttpResponse addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpResponse header(String key, String value) {
        if (value != null) {
            this.headers.put(key, value);
        }
        return this;
    }

    public HttpResponse status(int code) {
        this.status = HttpStatus.fromCode(code);
        return this;
    }

    public HttpResponse status(HttpStatus status) {
        this.status = status;
        return this;
    }

    public HttpResponse json(Object data) {
        if (data == null) return this;
        String json = io.github.jhanvi857.nioflow.util.JsonUtils.toJson(data);
        this.bodyBytes = json.getBytes(StandardCharsets.UTF_8);
        this.bodyLength = this.bodyBytes.length;
        this.headers.put("Content-Type", "application/json");
        this.headers.put("Content-Length", String.valueOf(this.bodyLength));
        return this;
    }

    public HttpResponse send(String body) {
        this.bodyBytes = body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0];
        this.bodyLength = this.bodyBytes.length;
        this.headers.put("Content-Type", "text/plain");
        this.headers.put("Content-Length", String.valueOf(this.bodyLength));
        return this;
    }

    public HttpResponse redirect(String url) {
        this.status = HttpStatus.fromCode(302);
        this.headers.put("Location", url);
        return this;
    }

    public HttpResponse redirect(String url, int code) {
        this.status = HttpStatus.fromCode(code);
        this.headers.put("Location", url);
        return this;
    }

    public HttpResponse setContentType(String contentType) {
        this.headers.put("Content-Type", contentType);
        return this;
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

        response.append("HTTP/1.1 ")
                .append(status.getCode())
                .append(" ")
                .append(status.getMessage())
                .append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        response.append("\r\n");

        out.write(response.toString().getBytes(StandardCharsets.UTF_8));

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
