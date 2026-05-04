package io.github.jhanvi857.nioflow.exception;

import io.github.jhanvi857.nioflow.Env;
import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global exception handler that converts unhandled exceptions into HTTP
 * responses.
 *
 * <p>
 * When {@code NIOFLOW_EXPOSE_ERROR_DETAILS} is {@code false} (the default),
 * no exception messages or stack traces are exposed in 500 response bodies.
 * Only structured, generic error messages are returned.
 * </p>
 */
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Whether to include exception messages in response bodies. Default: false. */
    private static final boolean EXPOSE_ERROR_DETAILS = Env.getAsBoolean("NIOFLOW_EXPOSE_ERROR_DETAILS", false);

    public static HttpResponse handle(Exception e) {
        logger.error("Unhandled exception processing request: {}", e.getMessage(), e);

        if (e instanceof UnsupportedMediaTypeException) {
            return new HttpResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "{\"error\": \"Unsupported Media Type\", \"message\": \"Content-Type must be application/json\"}");
        }

        if (e instanceof IllegalArgumentException) {
            String message = EXPOSE_ERROR_DETAILS ? e.getMessage() : "Invalid argument";
            return new HttpResponse(HttpStatus.BAD_REQUEST,
                    "{\"error\": \"Invalid argument\", \"message\": \"" + sanitizeForJson(message) + "\"}");
        }

        if (e instanceof SecurityException) {
            return new HttpResponse(HttpStatus.FORBIDDEN,
                    "{\"error\": \"Forbidden\", \"message\": \"Access denied\"}");
        }

        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "{\"error\": \"Internal Server Error\", \"message\": \"Something went wrong on our end\"}");
    }

    /**
     * Escapes characters that would break JSON string values.
     */
    private static String sanitizeForJson(String input) {
        if (input == null)
            return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
