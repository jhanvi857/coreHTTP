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

    // resolving route using longest prefix matching
    public RouteHandler resolve(HttpRequest request) {
        String path = request.getPath();

        // 1. Precise match
        if (routes.containsKey(path)) {
            return routes.get(path);
        }

        // 2. Longest Prefix Match
        String bestMatch = null;
        int bestLength = -1;

        for (String routePattern : routes.keySet()) {
            if (path.startsWith(routePattern)) {
                if (routePattern.length() > bestLength) {
                    bestLength = routePattern.length();
                    bestMatch = routePattern;
                }
            }
        }

        if (bestMatch != null) {
            return routes.get(bestMatch);
        }

        return null;
    }
}