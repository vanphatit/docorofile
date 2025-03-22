package com.group.docorofile.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToMany(mappedBy = "members")
    private Set<ChatRoomEntity> chatRooms = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageEntity> messagesSent = new ArrayList<>();

    private int downloadLimit;
    private boolean isChat;
    private boolean isComment;
    private String myProfile;
}