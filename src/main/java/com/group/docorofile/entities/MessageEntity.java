package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "messages")
public class MessageEntity implements Serializable {

    @Id
    private UUID messageId;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false) // Khóa ngoại trỏ đến MemberEntity
    private MemberEntity sender;

    @ManyToOne
    @JoinColumn(name = "chatroom_id", nullable = false) // Khóa ngoại trỏ đến ChatRoomEntity
    private ChatRoomEntity chatRoom;

    @Column(nullable = false, columnDefinition = "TEXT") // Lưu nội dung tin nhắn
    private String content;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (this.messageId == null) {
            this.messageId = UuidCreator.getTimeOrdered();
        }
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }
}
