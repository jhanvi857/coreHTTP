package io.github.jhanvi857.nioflow.routing;

import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.util.JsonUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class HttpContext {
    private final HttpRequest request;
    private final ExecutorService routeExecutor;
    private HttpResponse response;
    private String routePattern;
    private boolean dropResponse;

    public HttpContext(HttpRequest request) {
        this(request, null);
    }

    public HttpContext(HttpRequest request, ExecutorService routeExecutor) {
        this.request = request;
        this.routeExecutor = routeExecutor;
        // Default response is 200 OK, empty body
        this.response = new HttpResponse(HttpStatus.OK, "");
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public ExecutorService routeExecutor() {
        return routeExecutor;
    }

    // --- Request Helpers ---

    public String path() {
        return request.getPath();
    }

    public String method() {
        return request.getMethod();
    }

    public String remoteAddress() {
        return request.getRemoteAddress();
    }

    public String routePattern() {
        return routePattern != null ? routePattern : request.getPath();
    }

    public void setRoutePattern(String routePattern) {
        this.routePattern = routePattern;
    }

    public String header(String name) {
        return request.getHeaders().get(name);
    }

    public Map<String, String> headers() {
        return request.getHeaders();
    }

    public String query(String param) {
        // We will implement query extraction properly later.
        // For now, doing a basic string substring match if needed.
        return null;
    }

    private final java.util.Map<String, String> pathParams = new java.util.HashMap<>();

    public String pathParam(String param) {
        return pathParams.get(param);
    }

    public void addPathParam(String key, String value) {
        pathParams.put(key, value);
    }

    public Map<String, String> pathParams() {
        return java.util.Collections.unmodifiableMap(pathParams);
    }

    public <T> T body(Class<T> type) {
        String bodyString = request.getBodyAsString();
        if (bodyString == null || bodyString.isEmpty()) {
            return null;
        }
        return JsonUtils.fromJson(bodyString, type);
    }

    public String bodyAsString() {
        return request.getBodyAsString();
    }

    // --- Response Helpers ---

    public HttpContext status(HttpStatus status) {
        HttpResponse newResponse = new HttpResponse(status, response.getBody());
        response.getHeadersMap().forEach((k, v) -> {
            if (!k.equalsIgnoreCase("Content-Length")) {
                newResponse.addHeader(k, v);
            }
        });
        this.response = newResponse;
        return this;
    }

    public HttpContext status(int code) {
        return status(HttpStatus.fromCode(code));
    }

    public HttpContext header(String name, String value) {
        response.addHeader(name, value);
        return this;
    }

    public void send(String text) {
        HttpResponse newResponse = new HttpResponse(response.getStatus(), text);
        response.getHeadersMap().forEach((k, v) -> {
            if (!k.equalsIgnoreCase("Content-Length") && !k.equalsIgnoreCase("Content-Type")) {
                newResponse.addHeader(k, v);
            }
        });
        this.response = newResponse;
        this.response.addHeader("Content-Type", "text/plain; charset=UTF-8");
    }

    public void json(Object data) {
        String jsonString = JsonUtils.toJson(data);
        HttpResponse newResponse = new HttpResponse(response.getStatus(), jsonString);
        response.getHeadersMap().forEach((k, v) -> {
            if (!k.equalsIgnoreCase("Content-Length") && !k.equalsIgnoreCase("Content-Type")) {
                newResponse.addHeader(k, v);
            }
        });
        this.response = newResponse;
        this.response.addHeader("Content-Type", "application/json; charset=UTF-8");
    }

    public void dropResponse() {
        this.dropResponse = true;
    }

    public boolean isDropResponse() {
        return dropResponse;
    }

    public HttpContext fork() {
        HttpContext forked = new HttpContext(request, routeExecutor);
        forked.routePattern = this.routePattern;
        forked.pathParams.putAll(this.pathParams);
        return forked;
    }
}
