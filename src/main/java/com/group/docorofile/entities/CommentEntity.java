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
@SuperBuilder
@Table(name = "comments")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CommentEntity implements Serializable {
    @Id
    private UUID commentId;

    private String content;
    private boolean isLike;
    private boolean isDislike;

    @ManyToOne(optional = false)
    private MemberEntity author;

    @ManyToOne(optional = false)
    private DocumentEntity document;

    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    @PrePersist
    public void prePersist() {
        if (this.commentId == null) {
            this.commentId = UuidCreator.getTimeOrdered();
        }
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}