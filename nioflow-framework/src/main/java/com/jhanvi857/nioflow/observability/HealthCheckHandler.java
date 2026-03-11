package com.jhanvi857.nioflow.observability;

import com.jhanvi857.nioflow.protocol.HttpStatus;
import com.jhanvi857.nioflow.routing.HttpContext;
import com.jhanvi857.nioflow.routing.RouteHandler;

public class HealthCheckHandler implements RouteHandler {

    @Override
    public void handle(HttpContext ctx) throws Exception {
        long memoryUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;

        String status = "UP";
        HttpStatus httpStatus = HttpStatus.OK;

        String body = String.format(
                "{\"status\": \"%s\", \"memory_used_mb\": %d}",
                status, memoryUsed);

        ctx.status(httpStatus).send(body);
        ctx.header("Content-Type", "application/json");
    }
}
