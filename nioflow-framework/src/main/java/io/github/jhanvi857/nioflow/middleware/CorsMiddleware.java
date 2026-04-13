package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;

public class CorsMiddleware implements Middleware {
    private final String allowedOrigin;

    public CorsMiddleware(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        if (ctx.method().equalsIgnoreCase("OPTIONS")) {
            ctx.status(HttpStatus.OK).send("");
            addCorsHeaders(ctx);
            return;
        }

        next.handle(ctx);
        addCorsHeaders(ctx);
    }

    private void addCorsHeaders(HttpContext ctx) {
        ctx.header("Access-Control-Allow-Origin", allowedOrigin);
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, X-Auth-User");
        ctx.header("Access-Control-Max-Age", "3600");
    }
}
