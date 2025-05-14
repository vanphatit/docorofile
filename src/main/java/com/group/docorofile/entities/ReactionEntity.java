package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "reactions")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class ReactionEntity implements Serializable {
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

    @PreUpdate
    private void validateReaction() {
        if (this.isLike && this.isDislike) {
            throw new IllegalStateException("Không thể vừa like vừa dislike!");
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.reactionId == null) {
            this.reactionId = UuidCreator.getTimeOrdered();
        }

        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }

        if (this.modifiedOn == null) {
            this.modifiedOn = LocalDateTime.now();
        }

        if (this.isLike && this.isDislike) {
            throw new IllegalStateException("Không thể vừa like vừa dislike!");
        }
    }
}