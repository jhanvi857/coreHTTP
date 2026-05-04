package io.github.jhanvi857.nioflow.exception;

public class RequestHeaderFieldsTooLargeException extends RuntimeException {
    public RequestHeaderFieldsTooLargeException(String message) {
        super(message);
    }
}
