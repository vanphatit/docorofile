package com.group.docorofile.models.dto;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.CommentEntity;
import com.group.docorofile.entities.UniversityEntity;
import lombok.*;

import java.time.LocalDate;
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
    private String authorName;
    private String memberShip;
    private String courseName;
    private String universityName;
    private List<CommentDTO> comments;
    private String coverImageUrl;
    private String fileUrl;
    private int viewCount;
    private int likes;
    private int dislikes;
    private int views;
    private int favorites;
}

