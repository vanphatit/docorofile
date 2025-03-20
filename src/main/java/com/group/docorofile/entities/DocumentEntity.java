package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EDocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity implements Serializable {
    @Id
    private UUID documentId;
    private String title;
    private String description;
    private String fileUrl;
    @Enumerated(EnumType.STRING)
    private EDocumentStatus status;
    private LocalDateTime uploadedDate;
    private LocalDateTime modifiedOn;
    private int reportCount;
    private int viewCount;

    @ManyToOne(optional = false)
    private MemberEntity author;

    @ManyToOne(optional = false)
    private CourseEntity course;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    private List<CommentEntity> comments;

    @OneToMany(mappedBy = "reportedDoc", cascade = CascadeType.ALL)
    private List<ReportEntity> reports;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentViewEntity> viewers;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteListEntity> favoritedBy;

    @PrePersist
    public void prePersist() {
        if (this.documentId == null) {
            this.documentId = UuidCreator.getTimeOrdered();
        }
        if (this.uploadedDate == null) {
            this.uploadedDate = LocalDateTime.now();
        }
    }
}