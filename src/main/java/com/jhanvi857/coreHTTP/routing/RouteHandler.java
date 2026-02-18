package com.jhanvi857.coreHTTP.routing;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import java.io.IOException;

public interface RouteHandler {
    HttpResponse handle(HttpRequest request) throws IOException;
}