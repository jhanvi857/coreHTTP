package com.jhanvi857.nioflow.middleware;

import com.jhanvi857.nioflow.routing.HttpContext;
import com.jhanvi857.nioflow.routing.RouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(LoggerMiddleware.class);

    @Override
    public void process(HttpContext ctx, RouteHandler next) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            next.handle(ctx);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("{} {} - {} {}ms",
                    ctx.method(),
                    ctx.path(),
                    ctx.getResponse().getStatus().getCode(),
                    duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("{} {} - ERROR: {} {}ms",
                    ctx.method(),
                    ctx.path(),
                    e.getMessage(),
                    duration);
            throw e;
        }
    }
}
