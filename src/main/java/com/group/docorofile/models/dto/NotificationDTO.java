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
    private String title;
    private String author;
    private ENotificationType type;
    private String content;
    private boolean isSeen;
    private LocalDateTime createdOn;
    private LocalDateTime seenOn;
}
