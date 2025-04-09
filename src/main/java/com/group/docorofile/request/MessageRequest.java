package com.group.docorofile.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageRequest {

    @NotNull(message = "roomId is required")
    private UUID roomId;

    @NotBlank(message = "Content must not be blank")
    private String content;
}