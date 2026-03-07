package com.jhanvi857.coreHTTP.protocol;

public enum HttpStatus {
    // status codes.
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    NOT_FOUND(404, "Not Found"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
