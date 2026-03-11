package com.jhanvi857.nioflow.middleware;

import com.jhanvi857.nioflow.protocol.HttpStatus;
import com.jhanvi857.nioflow.routing.HttpContext;
import com.jhanvi857.nioflow.routing.RouteHandler;
import com.jhanvi857.nioflow.auth.JwtProvider;

public class AuthMiddleware implements Middleware {

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "Missing token"));
            return;
        }

        String token = authHeader.substring(7);

        if (JwtProvider.validateToken(token)) {
            ctx.getRequest().addHeader("X-Auth-User", JwtProvider.getUsernameFromToken(token));
            ctx.getRequest().addHeader("X-Auth-Role", JwtProvider.getRoleFromToken(token));
            next.handle(ctx);
        } else {
            ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "Invalid or expired token"));
        }
    }
}
