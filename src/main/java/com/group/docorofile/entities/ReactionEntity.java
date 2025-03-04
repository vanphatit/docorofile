package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "reactions")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ReactionEntity {
    @Id
    private UUID reactionId;

    @ManyToOne(optional = false)
    private MemberEntity author;

    @ManyToOne(optional = false)
    private DocumentEntity document;

    private boolean isLike;
    private boolean isDislike;

    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    @PrePersist
    public void prePersist() {
        if (this.reactionId == null) {
            this.reactionId = UuidCreator.getTimeOrdered();
        }

        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}