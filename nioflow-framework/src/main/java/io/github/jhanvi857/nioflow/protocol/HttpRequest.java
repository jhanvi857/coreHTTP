package io.github.jhanvi857.nioflow.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {
    private final String path;
    private final String method;
    private final String version;
    private final Map<String, String> headers;
    private final byte[] body;
    private String remoteAddress;

    public HttpRequest(String path, String method, String version, Map<String, String> headers, byte[] body) {
        this.path = path;
        this.method = method;
        this.version = version;
        // Ensure mutable headers
        this.headers = headers != null ? new java.util.HashMap<>(headers) : new java.util.HashMap<>();
        this.body = body != null ? body : new byte[0];
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
}
