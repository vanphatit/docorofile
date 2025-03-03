package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EMembershipLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipEntity {
    @Id
    private UUID membershipId = UuidCreator.getTimeOrdered();
    @Enumerated(EnumType.STRING)
    private EMembershipLevel level;
    private Date startDate;
    private Date endDate;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}