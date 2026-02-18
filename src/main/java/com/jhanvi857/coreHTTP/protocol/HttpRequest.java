package com.jhanvi857.coreHTTP.protocol;

import java.util.Map;

public class HttpRequest {
    private final String path;
    private final String method;
    private final String version;
    private final Map<String, String> headers;
    private final String body;
    public HttpRequest(String path,String method,String version,Map<String,String>headers,String body) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.version = version;
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

    public String getBody() {
        return body;
    }
}