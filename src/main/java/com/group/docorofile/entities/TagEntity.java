package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tags")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TagEntity {
    @Id
    private UUID tagId;

    private String tagName;

    @PrePersist
    public void prePersist() {
        if (this.tagId == null) {
            this.tagId = UuidCreator.getTimeOrdered();
        }
    }
}