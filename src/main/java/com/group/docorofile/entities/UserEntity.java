package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EUserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    private UUID userId = UuidCreator.getTimeOrdered();
    private String userName;
    private String email;
    private String password;
    private int downloadTurn;
    @Enumerated(EnumType.STRING)
    private EUserRole role;
    private Boolean isActive;
    private Boolean isChatManage;
    private Boolean isReportManage;
    private Boolean isChat;
    private Boolean isComment;
    private String myProfile;
    private Date createdOn;
    private Date modifiedOn;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private MembershipEntity membership;
}