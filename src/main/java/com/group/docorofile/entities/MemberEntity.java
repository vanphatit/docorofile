package com.group.docorofile.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "members")
public class MemberEntity extends UserEntity {
    @OneToOne
    private MembershipEntity membership;

    private int downloadLimit;
    private boolean isChat;
    private boolean isComment;
    private String myProfile;
}