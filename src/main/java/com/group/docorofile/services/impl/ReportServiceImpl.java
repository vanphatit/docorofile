package com.group.docorofile.services.impl;

import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.ReportEntity;
import com.group.docorofile.enums.ENotificationType;
import com.group.docorofile.enums.EReportStatus;
import com.group.docorofile.exceptions.DocumentNotFoundException;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.dto.ReportSummaryDTO;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.observer.NotificationCenter;
import com.group.docorofile.repositories.DocumentRepository;
import com.group.docorofile.repositories.ReportRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.request.CreateNotificationRequest;
import com.group.docorofile.request.CreateReportRequest;
import com.group.docorofile.services.iReportService;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements iReportService {
    private final ReportRepository reportRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(ReportRepository reportRepository, DocumentRepository documentRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }


    public void createReport(CreateReportRequest reportRequest) {
        DocumentEntity document = documentRepository.findById(reportRequest.getReportedDocId())
                .orElseThrow(() -> new DocumentNotFoundException("Tài liệu không tồn tại"));

        MemberEntity reporter = (MemberEntity) userRepository.findById(reportRequest.getReporterId())
                .orElseThrow(() -> new UserNotFoundException("Người báo cáo không tồn tại"));

        ReportEntity report = ReportEntity.builder()
                .reportId(UUID.randomUUID())
                .reportedDoc(document)
                .reporter(reporter)
                .status(EReportStatus.PENDING)
                .detail(reportRequest.getDetail())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        ReportEntity savedReport = reportRepository.save(report);

        // Tạo notification
        CreateNotificationRequest notiRequest = new CreateNotificationRequest();
        notiRequest.setReceiverId(reporter.getUserId());
        notiRequest.setType(ENotificationType.SYSTEM);
        notiRequest.setTitle("Send report success!");
        notiRequest.setContent("You have just sent a report to " + reportRequest.getReportedDocId() + " successfully.");

        // Gọi Observer
        NotificationCenter.notifyObservers(notiRequest);

        document.setReportCount(document.getReportCount() + 1);
        documentRepository.save(document);
    }

    public ResultPaginationDTO getTopReportedDocuments(Pageable pageable) {
        Page<ReportSummaryDTO> pageResult = reportRepository.findTopReportedDocuments(pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageResult.getNumber());
        meta.setPageSize(pageResult.getSize());
        meta.setPages(pageResult.getTotalPages());
        meta.setTotal(pageResult.getTotalElements());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(pageResult.getContent());

        return resultPaginationDTO;
    }

    @Transactional
    public void updateReportsStatusByDocumentId(UUID documentId, EReportStatus newStatus) {
        List<ReportEntity> reports = reportRepository.findByReportedDoc_DocumentId(documentId);

        for (ReportEntity report : reports) {
            report.setStatus(newStatus);
        }

        reportRepository.saveAll(reports);
    }

    public ResultPaginationDTO getReportDetailsByDocumentId(UUID documentId, Pageable pageable) {
        Page<String> pageResult = reportRepository.findReportDetailsByDocumentId(documentId, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageResult.getNumber());
        meta.setPageSize(pageResult.getSize());
        meta.setPages(pageResult.getTotalPages());
        meta.setTotal(pageResult.getTotalElements());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(pageResult.getContent());

        return resultPaginationDTO;
    }
}
