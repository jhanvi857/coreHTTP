package com.jhanvi857.nioflow;

import com.jhanvi857.nioflow.middleware.Middleware;
import com.jhanvi857.nioflow.routing.RouteHandler;
import com.jhanvi857.nioflow.routing.Router;
import com.jhanvi857.nioflow.server.HttpServer;

public class NioFlowApp {
    private final Router router;
    private HttpServer activeServer;

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

    public NioFlowApp onError(com.jhanvi857.nioflow.exception.ExceptionHandler handler) {
        return exception(Exception.class, handler);
    }

    public void listen(int port) {
        this.activeServer = new HttpServer(port);
        this.activeServer.start(router);
    }

    public void listenSecure(int port, String keystorePath, String password) {
        try {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("JKS");
            try (java.io.InputStream is = new java.io.FileInputStream(keystorePath)) {
                ks.load(is, password.toCharArray());
            }
            javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            this.activeServer = new HttpServer(port, sslContext);
            this.activeServer.start(router);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start secure server", e);
        }
    }

    public void drainAndStop(long timeout, java.util.concurrent.TimeUnit unit) {
        if (this.activeServer != null) {
            this.activeServer.drainAndStop(timeout, unit);
        }
    }
}
