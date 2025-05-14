package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.ENotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity implements Serializable {

    @Id
    private UUID notificationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id", referencedColumnName = "userId", nullable = false)
    private UserEntity receiver;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String author;

    private boolean isSeen;

    @Enumerated(EnumType.STRING)
    private ENotificationType type;

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
