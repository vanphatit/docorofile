package com.group.docorofile.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChatRoomResponse {
    private UUID id;
    private String title;
    private LocalDateTime createdOn;
    private int totalMembers;
}

