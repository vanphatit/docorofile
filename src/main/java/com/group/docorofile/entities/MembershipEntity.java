package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EMembershipLevel;
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
@Table(name = "membership")
public class MembershipEntity {
    @Id
    private UUID membershipId;

    @Enumerated(EnumType.STRING)
    private EMembershipLevel level;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @PrePersist
    public void prePersist() {
        if (this.membershipId == null) {
            this.membershipId = UuidCreator.getTimeOrdered();
        }
    }
}