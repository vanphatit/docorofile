package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MessageEntity {
    @Id
    private UUID messageId = UuidCreator.getTimeOrdered();
    private String content;
    private Date sentAt;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity sender;
}
