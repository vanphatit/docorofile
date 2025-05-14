package com.group.docorofile.request;

import com.group.docorofile.enums.ENotificationType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateNotificationRequest {
    private UUID receiverId;
    private ENotificationType type;
    private String title;
    private String content;
}