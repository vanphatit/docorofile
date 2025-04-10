package com.group.docorofile.response;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {

    public enum MessageType {
        CHAT, SYSTEM
    }

    private UUID id;
    private String content;
    private String sender;
    private UUID senderId;
    private UUID roomId;
    private String roomName;
    private LocalDateTime timestamp;
    private MessageType type;
}
