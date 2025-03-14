package com.group.docorofile.response;

import java.time.LocalDateTime;

public class CreatedResponse extends SuccessResponse {
    private static final int DEFAULT_STATUS = 201;
    private static final String DEFAULT_MESSAGE = "Created";

    public CreatedResponse(String message, Object data) {
        super(
                message != null ? message : DEFAULT_MESSAGE,
                DEFAULT_STATUS,
                data,
                LocalDateTime.now()
        );
    }
}