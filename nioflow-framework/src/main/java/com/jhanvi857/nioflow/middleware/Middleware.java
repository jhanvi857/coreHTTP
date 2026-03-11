package com.jhanvi857.nioflow.middleware;

import com.jhanvi857.nioflow.routing.HttpContext;
import com.jhanvi857.nioflow.routing.RouteHandler;

public interface Middleware {
    void process(HttpContext ctx, RouteHandler next) throws Exception;
}
