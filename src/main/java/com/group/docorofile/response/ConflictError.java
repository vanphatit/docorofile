package com.group.docorofile.response;

public class ConflictError extends ErrorResponse {
    private static final int DEFAULT_STATUS = 409;
    private static final String DEFAULT_MESSAGE = "Conflict";

    public ConflictError() {
        super(DEFAULT_MESSAGE, DEFAULT_STATUS);
    }

    public ConflictError(String message) {
        super(message, DEFAULT_STATUS);
    }
}