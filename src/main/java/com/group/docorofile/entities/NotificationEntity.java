package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.ENotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class NotificationEntity {
    @Id
    private UUID notificationId;

    @ManyToOne(optional = false)
    private UserEntity receiver;

    private String type;
    private String message;
    private boolean isSeen;

    private LocalDateTime createdOn;
    private LocalDateTime seenOn;

    @PrePersist
    public void prePersist() {
        if (this.notificationId == null) {
            this.notificationId = UuidCreator.getTimeOrdered();
        }
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}