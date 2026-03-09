package com.jhanvi857.coreHTTP.middleware;

import com.jhanvi857.coreHTTP.protocol.HttpRequest;
import com.jhanvi857.coreHTTP.protocol.HttpResponse;
import com.jhanvi857.coreHTTP.routing.RouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class LoggerMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(LoggerMiddleware.class);

    @Override
    public HttpResponse process(HttpRequest request, RouteHandler next) throws IOException {
        long startTime = System.currentTimeMillis();

        try {
            HttpResponse response = next.handle(request);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("{} {} - {} {}ms",
                    request.getMethod(),
                    request.getPath(),
                    response.getStatus().getCode(),
                    duration);

            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("{} {} - ERROR: {} {}ms",
                    request.getMethod(),
                    request.getPath(),
                    e.getMessage(),
                    duration);
            throw e;
        }
    }
}
