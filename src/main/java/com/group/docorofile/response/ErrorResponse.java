package com.group.docorofile.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"suppressed", "localizedMessage", "stackTrace", "cause"})
public class ErrorResponse extends RuntimeException {
    private final int statusCode;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponse(String message, int statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}