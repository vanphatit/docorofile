package com.group.docorofile.models.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private UUID messageId;
    private String content;
    private String createdAt;
    private boolean isMine;
    private SenderDTO sender;
}
