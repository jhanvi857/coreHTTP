package com.jhanvi857.coreHTTP.routing;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);
    private final Map<String, RouteHandler> routes = new HashMap<>();

    // registring route.
    public void register(String path, RouteHandler handler) {
        logger.debug("Registering route: {}", path);
        routes.put(path, handler);
    }

    // resolving route using longest prefix matching
    public RouteHandler resolve(HttpRequest request) {
        String path = request.getPath();
        logger.debug("Resolving route for path: {}", path);

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
            logger.debug("Best match for path {}: {}", path, bestMatch);
            return routes.get(bestMatch);
        }

        logger.info("No route found for path: {}", path);
        return null;
    }
}