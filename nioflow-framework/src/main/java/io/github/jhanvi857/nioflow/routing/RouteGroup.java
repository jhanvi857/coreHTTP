package io.github.jhanvi857.nioflow.routing;

import io.github.jhanvi857.nioflow.middleware.CircuitBreakerMiddleware;
import io.github.jhanvi857.nioflow.middleware.Middleware;
import java.util.ArrayList;
import java.util.List;

public class RouteGroup {
    private final String prefix;
    private final Router router;
    private final List<Middleware> groupMiddleware = new ArrayList<>();

    public RouteGroup(String prefix, Router router) {
        this.prefix = prefix;
        this.router = router;
    }

    public RouteGroup use(Middleware middleware) {
        if (middleware instanceof CircuitBreakerMiddleware) {
            ((CircuitBreakerMiddleware) middleware).groupKey(prefix);
        }
        this.groupMiddleware.add(middleware);
        return this;
    }

    public RouteRegistration get(String path, RouteHandler handler) {
        Route route = router.registerWithMiddleware("GET", combinePaths(prefix, path), handler, groupMiddleware);
        return new RouteRegistration(route);
    }

    public RouteRegistration post(String path, RouteHandler handler) {
        Route route = router.registerWithMiddleware("POST", combinePaths(prefix, path), handler, groupMiddleware);
        return new RouteRegistration(route);
    }

    public RouteRegistration put(String path, RouteHandler handler) {
        Route route = router.registerWithMiddleware("PUT", combinePaths(prefix, path), handler, groupMiddleware);
        return new RouteRegistration(route);
    }

    public RouteRegistration delete(String path, RouteHandler handler) {
        Route route = router.registerWithMiddleware("DELETE", combinePaths(prefix, path), handler, groupMiddleware);
        return new RouteRegistration(route);
    }

    private String combinePaths(String prefix, String path) {
        if (prefix.endsWith("/") && path.startsWith("/")) {
            return prefix + path.substring(1);
        } else if (!prefix.endsWith("/") && !path.startsWith("/")) {
            return prefix + "/" + path;
        }
        return prefix + path;
    }
}
