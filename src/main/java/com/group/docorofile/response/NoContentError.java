package com.group.docorofile.response;

public class NoContentError extends ErrorResponse {
    private static final int DEFAULT_STATUS = 204;
    private static final String DEFAULT_MESSAGE = "No Content";

    public NoContentError() {
        super(DEFAULT_MESSAGE, DEFAULT_STATUS);
    }

    public NoContentError(String message) {
        super(message, DEFAULT_STATUS);
    }
}