package io.github.jhanvi857.nioflow.exception;

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static HttpResponse handle(Exception e) {
        logger.error("Unhandled exception processing request: {}", e.getMessage(), e);

        if (e instanceof IllegalArgumentException) {
            return new HttpResponse(HttpStatus.BAD_REQUEST,
                    "{\"error\": \"Invalid argument\", \"message\": \"" + e.getMessage() + "\"}");
        }

        if (e instanceof SecurityException) {
            return new HttpResponse(HttpStatus.FORBIDDEN,
                    "{\"error\": \"Forbidden\", \"message\": \"Access denied\"}");
        }

        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "{\"error\": \"Internal Server Error\", \"message\": \"Something went wrong on our end\"}");
    }
}
