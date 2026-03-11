package com.jhanvi857.nioflow.routing;

import com.jhanvi857.nioflow.middleware.Middleware;
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
        this.groupMiddleware.add(middleware);
        return this;
    }

    public RouteGroup get(String path, RouteHandler handler) {
        router.registerWithMiddleware("GET", combinePaths(prefix, path), handler, groupMiddleware);
        return this;
    }

    public RouteGroup post(String path, RouteHandler handler) {
        router.registerWithMiddleware("POST", combinePaths(prefix, path), handler, groupMiddleware);
        return this;
    }

    public RouteGroup put(String path, RouteHandler handler) {
        router.registerWithMiddleware("PUT", combinePaths(prefix, path), handler, groupMiddleware);
        return this;
    }

    public RouteGroup delete(String path, RouteHandler handler) {
        router.registerWithMiddleware("DELETE", combinePaths(prefix, path), handler, groupMiddleware);
        return this;
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
