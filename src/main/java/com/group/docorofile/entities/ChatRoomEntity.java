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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chatrooms")
public class ChatRoomEntity implements Serializable {
    @Id
    private UUID chatId;

    private String title;
    private LocalDateTime createdOn;

    @ManyToOne
    private CourseEntity course;

    @PrePersist
    public void prePersist() {
        if (this.chatId == null) {
            this.chatId = UuidCreator.getTimeOrdered();
        }
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}