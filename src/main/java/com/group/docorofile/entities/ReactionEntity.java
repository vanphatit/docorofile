package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "reactions")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ReactionEntity {
    @Id
    private UUID reactionId = UuidCreator.getTimeOrdered();
    private boolean isLike;
    private boolean isDisLike;
    private Date createdOn;
    private Date modifiedOn;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}