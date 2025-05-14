package com.group.docorofile.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties({"suppressed", "localizedMessage", "stackTrace", "cause"})
public class ErrorResponse extends RuntimeException {
    private final int statusCode;
    private final String message;
    private final String timestamp;

    public ErrorResponse(String message, int statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
        // Format theo ISO-8601
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}