package io.github.jhanvi857.nioflow.exception;

/**
 * Thrown when a request body Content-Type does not match the expected media type.
 * Results in a 415 Unsupported Media Type response.
 */
public class UnsupportedMediaTypeException extends RuntimeException {
    public UnsupportedMediaTypeException(String message) {
        super(message);
    }
}
