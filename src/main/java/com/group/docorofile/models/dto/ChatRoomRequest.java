package com.group.docorofile.models.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChatRoomRequest {
    private String title;
    private UUID courseId;
}