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
        return null;
    }

    private final java.util.Map<String, String> pathParams = new java.util.HashMap<>();

    public String pathParam(String param) {
        return pathParams.get(param);
    }

    /**
     * Safely extracts a numeric path parameter, returning the parsed long value.
     * Throws IllegalArgumentException if the parameter is missing or non-numeric.
     * This prevents SQL injection when path params are passed to DB queries.
     *
     * @param param the path parameter name
     * @return the parsed long value
     * @throws IllegalArgumentException if missing or non-numeric
     */
    public long pathParamAsLong(String param) {
        String raw = pathParams.get(param);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing required path parameter: " + param);
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Path parameter '" + param + "' must be numeric, got: " + raw);
        }
    }

    /**
     * Safely extracts a numeric path parameter as int.
     */
    public int pathParamAsInt(String param) {
        String raw = pathParams.get(param);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing required path parameter: " + param);
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Path parameter '" + param + "' must be numeric, got: " + raw);
        }
    }

    public void addPathParam(String key, String value) {
        pathParams.put(key, value);
    }

    public Map<String, String> pathParams() {
        return java.util.Collections.unmodifiableMap(pathParams);
    }

    /**
     * Deserializes the request body as JSON into the given type.
     *
     * <p>
     * Validates that the Content-Type header starts with {@code application/json}
     * before attempting deserialization. Returns 415 Unsupported Media Type if the
     * Content-Type is missing or incorrect.
     * </p>
     */
    public <T> T body(Class<T> type) {
        String bodyString = request.getBodyAsString();
        if (bodyString == null || bodyString.isEmpty()) {
            return null;
        }

        String contentType = header("Content-Type");
        if (contentType == null || !contentType.toLowerCase(java.util.Locale.ROOT).contains("application/json")) {
            throw new io.github.jhanvi857.nioflow.exception.UnsupportedMediaTypeException(
                    "Content-Type must be application/json");
        }

        return JsonUtils.fromJson(bodyString, type);
    }

    public String bodyAsString() {
        return request.getBodyAsString();
    }

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
