package com.group.docorofile.services;

import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.models.dto.AdminDocumentDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface iDocumentService {
    DocumentEntity createDocument(DocumentEntity document);

    DocumentEntity updateDocument(UUID documentId, DocumentEntity updatedDocument);

    void deleteDocument(UUID documentId);

    DocumentEntity getDocument(UUID documentId);

    List<DocumentEntity> getAllDocuments();

    List<UserDocumentDTO> getAllUserDocuments();

    List<AdminDocumentDTO> getAllAdminDocuments();

    List<DocumentEntity> searchDocuments(String keyword);

    List<DocumentEntity> filterDocuments(UUID courseId, UUID universityId, LocalDateTime uploadDate, boolean sortByViews, boolean sortByLikes, boolean sortByDisLike, String status, boolean isAdmin);

    void updateStatus(UUID documentId, String status);

    void updateViews(UUID documentId, UUID memberId);

    int checkCountDocumentUploadInDay(UUID memberId);

    String uploadDocument(UUID memberId, MultipartFile file, String title, String description, String nameCourse, String nameUniversity);

    ResponseEntity<Resource> downloadDocument(UUID memberId, UUID documentId);

    List<DocumentEntity> getRelatedDocuments(UUID documentId);

    List<DocumentEntity> getRecommendedDocuments(UUID memberId);
}
