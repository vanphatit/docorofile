package com.group.docorofile.models.users;

import com.group.docorofile.enums.ENotificationType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateNotificationRequest {
    private UUID receiverId;
    private ENotificationType type;
    private String message;
}