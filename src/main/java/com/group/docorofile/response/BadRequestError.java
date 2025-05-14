package com.group.docorofile.response;

public class BadRequestError extends ErrorResponse {
    private static final int DEFAULT_STATUS = 400;
    private static final String DEFAULT_MESSAGE = "Bad Request";

    public BadRequestError() {
        super(DEFAULT_MESSAGE, DEFAULT_STATUS);
    }

    public BadRequestError(String message) {
        super(message, DEFAULT_STATUS);
    }
}