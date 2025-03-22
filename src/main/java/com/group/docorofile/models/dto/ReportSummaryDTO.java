package com.group.docorofile.models.dto;

import com.group.docorofile.enums.EReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportSummaryDTO {
    private UUID documentId;
    private String title;
    private EReportStatus status;
    private String urlDocument;
    private int reportCount;
}
