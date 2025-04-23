package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chatrooms")
public class ChatRoomEntity implements Serializable {
    @Id
    private UUID chatRoomId;

    private String title;
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageEntity> messages = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "chatroom_members", // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "chatroom_id"), // Khóa ngoại trỏ đến ChatRoom
            inverseJoinColumns = @JoinColumn(name = "member_id") // Khóa ngoại trỏ đến Member
    )
    private Set<MemberEntity> members = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.chatRoomId == null) {
            this.chatRoomId = UuidCreator.getTimeOrdered();
        }
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}