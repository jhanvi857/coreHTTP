package com.jhanvi857.coreHTTP.middleware;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.routing.RouteHandler;
import java.io.IOException;

public interface Middleware {
    HttpResponse process(HttpRequest request, RouteHandler next) throws IOException;
}
