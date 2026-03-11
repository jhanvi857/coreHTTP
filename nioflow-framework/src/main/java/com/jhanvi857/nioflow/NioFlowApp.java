package com.jhanvi857.nioflow;

import com.jhanvi857.nioflow.middleware.Middleware;
import com.jhanvi857.nioflow.routing.RouteHandler;
import com.jhanvi857.nioflow.routing.Router;
import com.jhanvi857.nioflow.server.HttpServer;

public class NioFlowApp {
    private final Router router;

    public NioFlowApp() {
        this.router = new Router();
    }

    public NioFlowApp get(String path, RouteHandler handler) {
        router.get(path, handler);
        return this;
    }

    public NioFlowApp post(String path, RouteHandler handler) {
        router.post(path, handler);
        return this;
    }

    public NioFlowApp put(String path, RouteHandler handler) {
        router.put(path, handler);
        return this;
    }

    public NioFlowApp delete(String path, RouteHandler handler) {
        router.delete(path, handler);
        return this;
    }

    public NioFlowApp use(Middleware middleware) {
        router.use(middleware);
        return this;
    }

    public NioFlowApp group(String prefix, java.util.function.Consumer<com.jhanvi857.nioflow.routing.RouteGroup> config) {
        com.jhanvi857.nioflow.routing.RouteGroup group = new com.jhanvi857.nioflow.routing.RouteGroup(prefix, router);
        config.accept(group);
        return this;
    }

    public NioFlowApp register(NioFlowPlugin plugin) {
        plugin.onRegister(this);
        return this;
    }

    public <T extends Exception> NioFlowApp exception(Class<T> exceptionClass,
            com.jhanvi857.nioflow.exception.ExceptionHandler handler) {
        router.addExceptionHandler(exceptionClass, handler);
        return this;
    }

    public void listen(int port) {
        new HttpServer(port).start(router);
    }
}
