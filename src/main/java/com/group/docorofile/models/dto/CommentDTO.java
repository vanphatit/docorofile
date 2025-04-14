package com.group.docorofile.models.dto;

import com.group.docorofile.entities.CommentEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    private UUID commentId;
    private String content;
    private LocalDateTime createdOn;
    private UUID authorId;
    private String authorName;
    private List<CommentDTO> replies = new ArrayList<>(); // Chỉ chứa cấp 2

    public static CommentDTO fromEntity(CommentEntity entity) {
        return CommentDTO.builder()
                .commentId(entity.getCommentId())
                .content(entity.getContent())
                .createdOn(entity.getCreatedOn())
                .authorId(entity.getAuthor().getUserId())
                .authorName(entity.getAuthor().getFullName())
                .build();
    }
}

