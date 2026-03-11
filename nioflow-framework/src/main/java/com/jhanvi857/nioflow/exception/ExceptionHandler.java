package com.jhanvi857.nioflow.exception;

import com.jhanvi857.nioflow.routing.HttpContext;

@FunctionalInterface
public interface ExceptionHandler {
    void handle(Exception e, HttpContext ctx);
}
