package com.group.docorofile.models.dto;

import com.group.docorofile.enums.ENotificationType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO implements Serializable {
    private UUID notificationId;
    private UUID receiverId;
    private ENotificationType type;
    private String message;
    private boolean isSeen;
    private LocalDateTime createdOn;
    private LocalDateTime seenOn;
}
