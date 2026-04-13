package io.github.jhanvi857.nioflow.routing;

public interface RouteHandler {
    void handle(HttpContext ctx) throws Exception;
}
