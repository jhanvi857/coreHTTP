package com.jhanvi857.coreHTTP.routing;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private final Map<String, RouteHandler> routes = new HashMap<>();

    // registring route.
    public void register(String path, RouteHandler handler) {
        routes.put(path, handler);
    }

    // resolving route.
    public RouteHandler resolve(HttpRequest request) {
        return routes.get(request.getPath());
    }
}