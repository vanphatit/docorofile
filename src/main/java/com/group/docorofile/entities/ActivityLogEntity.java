package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity_logs")
public class ActivityLogEntity {
    @Id
    private UUID logId;

    private String action;

    @ManyToOne(optional = false)
    private UserEntity actor;

    private String detail;
    private LocalDateTime createdOn;

    @PrePersist
    public void prePersist() {
        if (this.logId == null) {
            this.logId = UuidCreator.getTimeOrdered();
        }
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}