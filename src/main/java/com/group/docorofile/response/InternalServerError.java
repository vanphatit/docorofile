package com.group.docorofile.response;

public class InternalServerError extends ErrorResponse {
    private static final int DEFAULT_STATUS = 500;
    private static final String DEFAULT_MESSAGE = "Internal Server Error";

    public InternalServerError() {
        super(DEFAULT_MESSAGE, DEFAULT_STATUS);
    }

    public InternalServerError(String message) {
        super(message, DEFAULT_STATUS);
    }
}