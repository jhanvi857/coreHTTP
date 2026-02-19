package com.jhanvi857.coreHTTP.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {
    private final String path;
    private final String method;
    private final String version;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpRequest(String path, String method, String version, Map<String, String> headers, byte[] body) {
        this.path = path;
        this.method = method;
        this.version = version;
        this.headers = headers;
        this.body = body != null ? body : new byte[0];
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
}