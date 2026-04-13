package io.github.jhanvi857.nioflow.routing;

import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.util.JsonUtils;

import java.util.Map;

public class HttpContext {
    private final HttpRequest request;
    private HttpResponse response;

    public HttpContext(HttpRequest request) {
        this.request = request;
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

    // --- Request Helpers ---

    public String path() {
        return request.getPath();
    }

    public String method() {
        return request.getMethod();
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
        this.response = new HttpResponse(status, response.getBody());
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
        this.response = new HttpResponse(response.getStatus(), text);
        this.response.addHeader("Content-Type", "text/plain; charset=UTF-8");
    }

    public void json(Object data) {
        String jsonString = JsonUtils.toJson(data);
        this.response = new HttpResponse(response.getStatus(), jsonString);
        this.response.addHeader("Content-Type", "application/json; charset=UTF-8");
    }
}
