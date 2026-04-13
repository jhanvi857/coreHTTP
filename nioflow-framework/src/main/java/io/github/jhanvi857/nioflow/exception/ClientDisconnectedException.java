package io.github.jhanvi857.nioflow.exception;

public class ClientDisconnectedException extends RuntimeException {
    public ClientDisconnectedException(String message) {
        super(message);
    }
}
