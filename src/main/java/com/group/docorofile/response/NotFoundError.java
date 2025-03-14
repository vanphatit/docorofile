package com.group.docorofile.response;

public class NotFoundError extends ErrorResponse {
    private static final int DEFAULT_STATUS = 404;
    private static final String DEFAULT_MESSAGE = "Not Found";

    public NotFoundError() {
        super(DEFAULT_MESSAGE, DEFAULT_STATUS);
    }

    public NotFoundError(String message) {
        super(message, DEFAULT_STATUS);
    }
}