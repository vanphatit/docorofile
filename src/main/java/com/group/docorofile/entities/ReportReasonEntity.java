package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import com.group.docorofile.enums.EReasonType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "report_reasons")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ReportReasonEntity {
    @Id
    private UUID reasonId;

    private String reasonName;

    @Enumerated(EnumType.STRING)
    private EReasonType reasonType;

    private String description;
    private boolean isActive;

    @PrePersist
    public void prePersist() {
        if (this.reasonId == null) {
            this.reasonId = UuidCreator.getTimeOrdered();
        }
    }
}