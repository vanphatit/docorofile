package com.group.docorofile.services;

import com.group.docorofile.enums.EReportStatus;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.request.CreateReportRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface iReportService {
    void createReport(CreateReportRequest reportRequest);
    ResultPaginationDTO getTopReportedDocuments(Pageable pageable);
    void updateReportsStatusByDocumentId(UUID documentId, EReportStatus newStatus);
    ResultPaginationDTO getReportDetailsByDocumentId(UUID documentId, Pageable pageable);
}
