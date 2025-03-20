package com.group.docorofile.models.dto;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.CommentEntity;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    private UUID documentId;
    private String title;
    private String description;
    private LocalDateTime uploadedDate;
    private int viewCount;
    private MemberEntity author;
    private CourseEntity course;
    private List<CommentEntity> comments;
    private String coverImageUrl;
}

