package com.group.docorofile.models.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDTO {
    private UUID reportId;
    private UUID reportedDocId;
    private UUID reporterId;
    private String title;
    private String status;
    private String detail;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
