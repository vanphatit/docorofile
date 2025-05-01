package com.group.docorofile.services;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.models.dto.AdminDocumentDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
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

    Object viewDocumentByIdForUI(UUID documentId, String role, String userName);

    Object searchDocuments(String keyword, String role);

    Object filterDocuments(UUID courseId, UUID universityId, LocalDateTime uploadDate, boolean sortByViews, boolean sortByLikes, boolean sortByDisLike, String status, boolean isAdmin);

    void updateStatus(UUID documentId, String status);

    boolean softDeleteDocument(UUID documentId);

    void updateViews(UUID documentId, UUID memberId);

    int checkCountDocumentUploadInDay(UUID memberId);

    String uploadDocument(UUID memberId, MultipartFile file, String title, String description, String nameCourse, String nameUniversity);

    UUID saveFileOnly(UUID memberId, MultipartFile file);

    UserDocumentDTO updateMetadata(UUID documentId, String title, String description, String nameCourse, String nameUniversity);

    ResponseEntity<Resource> downloadDocument(UUID memberId, UUID documentId);

    Page<DocumentEntity> getRelatedDocuments(UUID documentId, int page, int size);

    Page<UserDocumentDTO> getRelatedDocumentsForUI(UUID documentId, int page, int size);

    Page<DocumentEntity> getRecommendedDocuments(UUID memberId, int page, int size);

    Page<UserDocumentDTO> getRecommendedDocumentsForUI(UUID memberId, int page, int size);

    Page<DocumentEntity> getHistoryDocuments(UUID memberId, int page, int size);

    Page<UserDocumentDTO> getHistoryDocumentsForUI(UUID memberId, int page, int size);

    List<DocumentEntity> getDocumentByCourseId(UUID courseId);

    List<DocumentEntity> getDocumentByUniversityId(String univName);

    List<DocumentEntity> getDocumentByUniversityAndCourse(String univName, String courseName);

    Page<DocumentEntity> getDocumentByCourseAndFollowedByMember(UUID memberId, int page, int size);

    Page<UserDocumentDTO> getDocumentByCourseAndFollowedByMemberForUI(UUID memberId, int page, int size);

    Page<UserDocumentDTO> getDocumentByAuthor(UUID authorId, int page, int size);

    List<CourseEntity> findAllWithUniversity();
}
