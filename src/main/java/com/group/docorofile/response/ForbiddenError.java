package com.group.docorofile.response;

public class ForbiddenError extends ErrorResponse {
    private static final int DEFAULT_STATUS = 403;
    private static final String DEFAULT_MESSAGE = "Forbidden";

    public ForbiddenError() {
        super(DEFAULT_MESSAGE, DEFAULT_STATUS);
    }

    public ForbiddenError(String message) {
        super(message, DEFAULT_STATUS);
    }
}