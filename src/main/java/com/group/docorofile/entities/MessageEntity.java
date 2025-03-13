package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
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
    private MemberEntity sender;

    @ManyToOne
    private ChatRoomEntity chatRoom;

    private String content;
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
