package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ReportEntity {
    @Id
    private UUID reportId = UuidCreator.getTimeOrdered();
    @Enumerated(EnumType.STRING)
    private EReportStatus status;
    private String detail;
    private Date createdOn;
    private Date modifiedOn;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentEntity reportedDoc;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity reporter;
}