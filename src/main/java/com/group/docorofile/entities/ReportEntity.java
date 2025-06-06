package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EReportStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class ReportEntity implements Serializable {
    @Id
    private UUID reportId;

    @ManyToOne(optional = false)
    private DocumentEntity reportedDoc;

    @ManyToOne(optional = false)
    private MemberEntity reporter;

    @Enumerated(EnumType.STRING)
    private EReportStatus status;

    private String detail;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    @PrePersist
    public void prePersist() {
        if (this.reportId == null) {
            this.reportId = UuidCreator.getTimeOrdered();
        }
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}