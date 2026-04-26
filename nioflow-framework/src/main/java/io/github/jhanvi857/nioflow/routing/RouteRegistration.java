package io.github.jhanvi857.nioflow.routing;

/**
 * Fluent route registration handle for per-route behavior.
 */
public class RouteRegistration {
    private final Route route;

    public RouteRegistration(Route route) {
        this.route = route;
    }

    public RouteRegistration timeout(int ms) {
        route.timeout(ms);
        return this;
    }

    public RouteRegistration rateLimit(int requests, int windowMs) {
        route.rateLimit(requests, windowMs);
        return this;
    }

    public RouteRegistration hedge(int delayMs) {
        route.hedge(delayMs);
        return this;
    }

    public RouteRegistration use(io.github.jhanvi857.nioflow.middleware.Middleware middleware) {
        route.wrap(middleware);
        return this;
    }
}
