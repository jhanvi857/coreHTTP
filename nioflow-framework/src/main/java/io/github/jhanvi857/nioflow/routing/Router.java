package io.github.jhanvi857.nioflow.routing;

import io.github.jhanvi857.nioflow.exception.ExceptionHandler;
import io.github.jhanvi857.nioflow.exception.GlobalExceptionHandler;
import io.github.jhanvi857.nioflow.protocol.HttpRequest;
import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.middleware.Middleware;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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

    public Route get(String path, RouteHandler handler) {
        return register("GET", path, handler);
    }

    public Route post(String path, RouteHandler handler) {
        return register("POST", path, handler);
    }

    public Route put(String path, RouteHandler handler) {
        return register("PUT", path, handler);
    }

    public Route delete(String path, RouteHandler handler) {
        return register("DELETE", path, handler);
    }

    public Route register(String method, String path, RouteHandler handler) {
        return registerWithMiddleware(method, path, handler, Collections.emptyList());
    }

    public Route registerWithMiddleware(String method, String path, RouteHandler handler,
            List<Middleware> scopedMiddleware) {
        logger.debug("Registering route: {} {}", method, path);
        List<Middleware> allMiddleware = new ArrayList<>(globalMiddleware);
        allMiddleware.addAll(scopedMiddleware);

        RouteHandler finalHandler = wrapHandler(handler, allMiddleware);
        Route route = new Route(method, path, finalHandler);
        routes.add(route);
        return route;
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

    public HttpContext dispatch(HttpRequest request, ExecutorService routeExecutor) {
        Route route = resolve(request.getMethod(), request.getPath());
        HttpContext ctx = new HttpContext(request, routeExecutor);

        if (route == null) {
            HttpResponse notFound = new HttpResponse(HttpStatus.NOT_FOUND, "<h1>404 Not Found</h1>");
            ctx.setResponse(notFound);
            return ctx;
        }

        route.extractPathParams(request.getPath()).forEach(ctx::addPathParam);
        ctx.setRoutePattern(route.key());

        try {
            route.execute(ctx, routeExecutor);
            return ctx;
        } catch (Exception e) {
            ExceptionHandler handler = getExceptionHandler(e.getClass());
            if (handler != null) {
                try {
                    handler.handle(e, ctx);
                } catch (Exception handlerException) {
                    ctx.setResponse(GlobalExceptionHandler.handle(handlerException));
                }
            } else {
                ctx.setResponse(GlobalExceptionHandler.handle(e));
            }
            return ctx;
        }
    }
}
