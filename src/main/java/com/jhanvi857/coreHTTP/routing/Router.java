package com.jhanvi857.coreHTTP.routing;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
// import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.middleware.Middleware;
// import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);
    // Map<Method, Map<Path, Handler>>
    private final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();
    private final List<Middleware> globalMiddleware = new ArrayList<>();

    public void use(Middleware middleware) {
        globalMiddleware.add(middleware);
    }

    public void get(String path, RouteHandler handler) {
        register("GET", path, handler);
    }

    public void post(String path, RouteHandler handler) {
        register("POST", path, handler);
    }

    public void put(String path, RouteHandler handler) {
        register("PUT", path, handler);
    }

    public void delete(String path, RouteHandler handler) {
        register("DELETE", path, handler);
    }

    public void register(String method, String path, RouteHandler handler) {
        logger.debug("Registering route: {} {}", method, path);
        routes.computeIfAbsent(method.toUpperCase(), k -> new HashMap<>())
                .put(path, wrapHandler(handler));
    }

    private RouteHandler wrapHandler(RouteHandler finalHandler) {
        return request -> {
            RouteHandler current = finalHandler;
            for (int i = globalMiddleware.size() - 1; i >= 0; i--) {
                Middleware m = globalMiddleware.get(i);
                RouteHandler inner = current;
                current = req -> m.process(req, inner);
            }
            return current.handle(request);
        };
    }

    // resolving route using longest prefix matching
    public RouteHandler resolve(HttpRequest request) {
        String method = request.getMethod().toUpperCase();
        String path = request.getPath();
        logger.debug("Resolving route for: {} {}", method, path);

        Map<String, RouteHandler> methodRoutes = routes.get(method);
        if (methodRoutes == null) {
            logger.info("No routes registered for method: {}", method);
            return null;
        }

        // 1. Precise match
        if (methodRoutes.containsKey(path)) {
            return methodRoutes.get(path);
        }

        // 2. Longest Prefix Match
        String bestMatch = null;
        int bestLength = -1;

        for (String routePattern : methodRoutes.keySet()) {
            if (path.startsWith(routePattern)) {
                if (routePattern.length() > bestLength) {
                    bestLength = routePattern.length();
                    bestMatch = routePattern;
                }
            }
        }

        if (bestMatch != null) {
            logger.debug("Best match for path {}: {}", path, bestMatch);
            return methodRoutes.get(bestMatch);
        }

        logger.info("No route found for {} {}", method, path);
        return null;
    }
}