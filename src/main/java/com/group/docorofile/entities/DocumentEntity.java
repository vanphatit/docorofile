package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EDocumentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {
    @Id
    private UUID documentId = UuidCreator.getTimeOrdered();
    private String title;
    private String description;
    private String fileUrl;
    @Enumerated(EnumType.STRING)
    private EDocumentStatus status;
    private int reportCount;
    private Date uploadedDate;
    private Date modifiedOn;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}