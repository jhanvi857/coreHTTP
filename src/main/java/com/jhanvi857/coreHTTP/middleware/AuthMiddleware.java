package com.jhanvi857.coreHTTP.middleware;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;
import com.jhanvi857.coreHTTP.routing.RouteHandler;
import com.jhanvi857.coreHTTP.auth.JwtProvider;
import java.io.IOException;

public class AuthMiddleware implements Middleware {

    @Override
    public HttpResponse process(HttpRequest request, RouteHandler next) throws IOException {
        String authHeader = request.getHeaders().get("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new HttpResponse(HttpStatus.UNAUTHORIZED, "{\"error\": \"Missing token\"}");
        }

        String token = authHeader.substring(7);

        if (JwtProvider.validateToken(token)) {
            request.addHeader("X-Auth-User", JwtProvider.getUsernameFromToken(token));
            request.addHeader("X-Auth-Role", JwtProvider.getRoleFromToken(token));
            return next.handle(request);
        } else {
            return new HttpResponse(HttpStatus.UNAUTHORIZED, "{\"error\": \"Invalid or expired token\"}");
        }
    }
}
