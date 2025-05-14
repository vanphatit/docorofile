package com.group.docorofile.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @ManyToOne(optional = false)
    private MemberEntity author;

    @ManyToOne(optional = false)
    private DocumentEntity document;

    private LocalDateTime createdOn;
    private LocalDateTime deletedOn;

    // Quan hệ đệ quy: Bình luận có thể có cha (optional)
    @ManyToOne(optional = true)
    @JoinColumn(name = "parent_comment_id")
    @ToString.Exclude
    @JsonBackReference
    private CommentEntity parentComment;

    // Danh sách các bình luận con (replies)
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CommentEntity> replies = new ArrayList<>();

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