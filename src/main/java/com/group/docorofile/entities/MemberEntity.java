package com.group.docorofile.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentViewEntity> docViewed;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FollowCourseEntity> followedCourses;

    private int downloadLimit;
    private boolean isChat;
    private boolean isComment;
    private String myProfile;
}