package com.group.docorofile.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {
    private String message;
    private int statusCode;
    private Object data;
    private LocalDateTime timestamp;
}