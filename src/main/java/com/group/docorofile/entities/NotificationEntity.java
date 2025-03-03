package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.ENotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class NotificationEntity {
    @Id
    private UUID notificationId = UuidCreator.getTimeOrdered();
    @Enumerated(EnumType.STRING)
    private ENotificationType type;
    private boolean isSeen;
    private String message;
    private Date createdOn;
    private Date seenDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}