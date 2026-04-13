package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;

public interface Middleware {
    void process(HttpContext ctx, RouteHandler next) throws Exception;
}
