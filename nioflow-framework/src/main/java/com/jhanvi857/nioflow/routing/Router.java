package com.jhanvi857.nioflow.routing;

import com.jhanvi857.nioflow.exception.ExceptionHandler;
import com.jhanvi857.nioflow.middleware.Middleware;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);
    private final List<Route> routes = new ArrayList<>();
    private final List<Middleware> globalMiddleware = new ArrayList<>();
    private final Map<Class<? extends Exception>, ExceptionHandler> exceptionHandlers = new HashMap<>();

    public void addExceptionHandler(Class<? extends Exception> exceptionClass, ExceptionHandler handler) {
        exceptionHandlers.put(exceptionClass, handler);
    }

    public ExceptionHandler getExceptionHandler(Class<? extends Throwable> exceptionClass) {
        Class<?> current = exceptionClass;
        while (current != null && Throwable.class.isAssignableFrom(current)) {
            ExceptionHandler handler = exceptionHandlers.get(current);
            if (handler != null) {
                return handler;
            }
            current = current.getSuperclass();
        }
        return null;
    }

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
        registerWithMiddleware(method, path, handler, Collections.emptyList());
    }

    public void registerWithMiddleware(String method, String path, RouteHandler handler,
            List<Middleware> scopedMiddleware) {
        logger.debug("Registering route: {} {}", method, path);
        List<Middleware> allMiddleware = new ArrayList<>(globalMiddleware);
        allMiddleware.addAll(scopedMiddleware);

        RouteHandler finalHandler = wrapHandler(handler, allMiddleware);
        routes.add(new Route(method, path, finalHandler));
    }

    private RouteHandler wrapHandler(RouteHandler finalHandler, List<Middleware> middlewareList) {
        return ctx -> {
            RouteHandler current = finalHandler;
            for (int i = middlewareList.size() - 1; i >= 0; i--) {
                Middleware m = middlewareList.get(i);
                RouteHandler inner = current;
                current = c -> m.process(c, inner);
            }
            current.handle(ctx);
        };
    }

    public Route resolve(String method, String path) {
        logger.debug("Resolving route for: {} {}", method, path);
        for (Route route : routes) {
            if (route.matches(method, path)) {
                return route;
            }
        }
        logger.info("No route found for {} {}", method, path);
        return null;
    }
}