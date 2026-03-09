package com.jhanvi857.coreHTTP.middleware;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.protocol.HttpStatus;
import com.jhanvi857.coreHTTP.routing.RouteHandler;
import java.io.IOException;

public class CorsMiddleware implements Middleware {
    private final String allowedOrigin;

    public CorsMiddleware(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public HttpResponse process(HttpRequest request, RouteHandler next) throws IOException {
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            HttpResponse response = new HttpResponse(HttpStatus.OK, "");
            addCorsHeaders(response);
            return response;
        }

        HttpResponse response = next.handle(request);
        addCorsHeaders(response);
        return response;
    }

    private void addCorsHeaders(HttpResponse response) {
        response.addHeader("Access-Control-Allow-Origin", allowedOrigin);
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.addHeader("Access-Control-Max-Age", "3600");
    }
}
