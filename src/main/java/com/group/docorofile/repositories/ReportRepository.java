package com.group.docorofile.repositories;

import com.group.docorofile.entities.ReportEntity;
import com.group.docorofile.models.dto.ReportSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {
    @Query("""
    SELECT new com.group.docorofile.models.dto.ReportSummaryDTO(
        r.reportedDoc.documentId,
        d.title,
        r.status,
        d.fileUrl,
        d.reportCount
    )
    FROM ReportEntity r
    JOIN DocumentEntity d ON d.documentId = r.reportedDoc.documentId
    WHERE r.createdOn = (
        SELECT MAX(r2.createdOn) 
        FROM ReportEntity r2 
        WHERE r2.reportedDoc.documentId = r.reportedDoc.documentId
    )
    ORDER BY d.reportCount DESC
    """)
    Page<ReportSummaryDTO> findTopReportedDocuments(Pageable pageable);

    List<ReportEntity> findByReportedDoc_DocumentId(UUID documentId);
}
