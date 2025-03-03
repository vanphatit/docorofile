package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "chats")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ChatEntity {
    @Id
    private UUID chatId = UuidCreator.getTimeOrdered();
    private String title;
    private Date createdOn;
}