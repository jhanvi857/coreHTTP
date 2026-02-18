package com.jhanvi857.coreHTTP.exception;

public class ClientDisconnectedException extends RuntimeException {
    public ClientDisconnectedException(String message) {
        super(message);
    }
}
