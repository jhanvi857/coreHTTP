package io.github.jhanvi857.nioflow.exception;

import io.github.jhanvi857.nioflow.routing.HttpContext;

@FunctionalInterface
public interface ExceptionHandler {
    void handle(Exception e, HttpContext ctx);
}
