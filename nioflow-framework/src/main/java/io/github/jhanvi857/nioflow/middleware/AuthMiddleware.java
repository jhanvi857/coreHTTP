package io.github.jhanvi857.nioflow.middleware;

import io.github.jhanvi857.nioflow.Env;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import io.github.jhanvi857.nioflow.routing.HttpContext;
import io.github.jhanvi857.nioflow.routing.RouteHandler;
import io.github.jhanvi857.nioflow.auth.JwtProvider;

public class AuthMiddleware implements Middleware {

    public AuthMiddleware() {
        if (Env.getAsBoolean("NIOFLOW_DISABLE_AUTH", false)) {
            String bindAddress = Env.get("NIOFLOW_BIND_ADDRESS", "127.0.0.1");
            boolean isLoopback = bindAddress.equals("127.0.0.1") || bindAddress.equals("localhost") || bindAddress.equals("::1");
            if (!isLoopback) {
                throw new IllegalStateException("NIOFLOW_DISABLE_AUTH=true is only allowed when binding to loopback address!");
            }
        }
    }

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        if (Env.getAsBoolean("NIOFLOW_DISABLE_AUTH", false)) {
            next.handle(ctx);
            return;
        }

        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "Missing token"));
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (JwtProvider.validateToken(token)) {
                ctx.getRequest().addHeader("X-Auth-User", JwtProvider.getUsernameFromToken(token));
                ctx.getRequest().addHeader("X-Auth-Role", JwtProvider.getRoleFromToken(token));
                next.handle(ctx);
            } else {
                ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "Invalid or expired token"));
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "Token validation failed"));
        }
    }
}
