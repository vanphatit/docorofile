package com.group.docorofile.services.impl;

import com.group.docorofile.entities.*;
import com.group.docorofile.enums.EDocumentStatus;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.models.dto.AdminDocumentDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.models.mappers.DocumentMapper;
import com.group.docorofile.repositories.*;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.services.iDocumentService;
import com.group.docorofile.services.specifications.DocumentSpecification;
import com.group.docorofile.services.utils.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements iDocumentService {
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentViewRepository documentViewRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Override
    public DocumentEntity createDocument(DocumentEntity document) {
        document.setUploadedDate(LocalDateTime.now());
        return documentRepository.save(document);
    }

    @Override
    public DocumentEntity updateDocument(UUID documentId, DocumentEntity updatedDocument) {
        return documentRepository.findById(documentId).map(document -> {
            document.setTitle(updatedDocument.getTitle());
            document.setDescription(updatedDocument.getDescription());
            document.setModifiedOn(LocalDateTime.now());
            return documentRepository.save(document);
        }).orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));
    }

    @Override
    public void deleteDocument(UUID documentId) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));
        documentRepository.delete(document);
    }

    @Override
    public DocumentEntity getDocument(UUID documentId) {
        return documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));
    }

    @Override
    public List<DocumentEntity> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public List<UserDocumentDTO> getAllUserDocuments() {
        return documentRepository.findAll().stream().map(DocumentMapper::toUserDTO).collect(Collectors.toList());
    }

    @Override
    public List<AdminDocumentDTO> getAllAdminDocuments() {
        return documentRepository.findAll().stream().map(DocumentMapper::toAdminDTO).collect(Collectors.toList());
    }

    @Override
    public Object viewDocumentByIdForUI(UUID documentId, String role, String userName) {
        // Kiểm tra tài liệu có tồn tại không
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundError("Tài liệu không tồn tại!"));

        // Tăng viewCount
        document.setViewCount(document.getViewCount() + 1);
        documentRepository.save(document);

        try {
            if (role.equals("ROLE_MEMBER")) {
                // Kiểm tra xem user này đã xem tài liệu chưa
                MemberEntity currentMem = (MemberEntity) userRepository.findByEmail(userName).get();
                boolean alreadyViewed = documentViewRepository.existsByDocument_DocumentIdAndMember_UserId(documentId, currentMem.getUserId());

                if (!alreadyViewed) {
                    // Ghi lại lượt xem vào DocumentViewEntity
                    DocumentViewEntity view = DocumentViewEntity.builder()
                            .document(document)
                            .member(currentMem)
                            .viewedAt(LocalDateTime.now())
                            .downloadedAt(null)
                            .build();

                }
            }
            // Kiểm tra vai trò từ token
            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MODERATOR")) {
                return DocumentMapper.toAdminDTO(document);
            } else {
                return DocumentMapper.toUserDTO(document);
            }
        } catch (RuntimeException e) {
            return new UnauthorizedError(e.getMessage());
        }
    }

    @Override
    public Object searchDocuments(String keyword, String role) {
        try {
            List<DocumentEntity> documents = documentRepository.findAll(DocumentSpecification.searchByKeyword(keyword));
            if (documents.isEmpty()) {
                return new NotFoundError("Không tìm thấy tài liệu nào!");
            }

            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MODERATOR")) {
                return documents.stream().map(DocumentMapper::toAdminDTO).collect(Collectors.toList());
            } else {
                return documents.stream().map(DocumentMapper::toUserDTO).collect(Collectors.toList());
            }
        } catch (RuntimeException e) {
            return new UnauthorizedError(e.getMessage());
        }
    }

    @Override
    public Object filterDocuments(UUID courseId, UUID universityId, LocalDateTime uploadDate, boolean sortByViews, boolean sortByLikes, boolean sortByDisLike, String status, boolean isAdmin) {
        try {
            List<DocumentEntity> documents = documentRepository.findAll(DocumentSpecification.filterDocuments(courseId, universityId, uploadDate, sortByViews, sortByLikes, sortByDisLike, status, isAdmin));
            if (isAdmin) {
                return documents.stream().map(DocumentMapper::toAdminDTO).collect(Collectors.toList());
            } else {
                return documents.stream().map(DocumentMapper::toUserDTO).collect(Collectors.toList());
            }
        } catch (RuntimeException e) {
            return new UnauthorizedError(e.getMessage());
        }
    }

    @Override
    public void updateStatus(UUID documentId, String status) {
        documentRepository.findById(documentId).map(document -> {
            document.setStatus(EDocumentStatus.valueOf(status));
            document.setModifiedOn(LocalDateTime.now());
            return documentRepository.save(document);
        }).orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));
    }

    @Override
    public boolean softDeleteDocument(UUID documentId) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));
        document.setStatus(EDocumentStatus.DELETED);
        documentRepository.save(document);
        return true;
    }

    @Override
    public void updateViews(UUID documentId, UUID memberId) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));

        MemberEntity member = (MemberEntity) userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Kiểm tra xem user này đã xem tài liệu chưa
        boolean alreadyViewed = documentViewRepository.existsByDocument_DocumentIdAndMember_UserId(documentId, memberId);

        if (!alreadyViewed) {
            // Tăng viewCount
            document.setViewCount(document.getViewCount() + 1);
            documentRepository.save(document);

            // Ghi lại lượt xem vào DocumentViewEntity
            DocumentViewEntity view = DocumentViewEntity.builder()
                    .document(document)
                    .member(member)
                    .viewedAt(LocalDateTime.now())
                    .build();
            documentViewRepository.save(view);
        }
    }

    @Override
    public int checkCountDocumentUploadInDay(UUID memberId) {
        return documentRepository.countDocumentUploadInDay(LocalDateTime.now().toLocalDate(), memberId);
    }

    @Override
    public String uploadDocument(UUID memberId, MultipartFile file, String title, String description, String nameCourse, String nameUniversity) {
        // Kiểm tra xem người dùng có tồn tại không
        MemberEntity member = (MemberEntity) userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (!courseRepository.existsByCourseName(nameCourse)) {
            return "Không tìm thấy khóa học!";
        }

        if (!universityRepository.existsByUnivName(nameUniversity)) {
            return "Không tìm thấy trường học!";
        }

        // Kiểm tra tài liệu trùng của chính user
        if (documentRepository.existsByTitleAndAuthor_UserId(title, memberId)) {
            return "Bạn đã tải lên tài liệu này trước đó. Hãy cập nhật thay vì tạo mới.";
        }

        String fileUrl;
        try {
            // Lưu file sử dụng FileStorageService
            fileUrl = fileStorageService.saveFile(file);
        } catch (IOException e) {
            return "Lỗi khi lưu tài liệu! Vui lòng thử lại.";
        }

        // Lưu vào database
        DocumentEntity document = DocumentEntity.builder()
                .title(title)
                .description(description)
                .uploadedDate(LocalDateTime.now())
                .author(member)
                .fileUrl(fileUrl) // Lưu đường dẫn từ FileStorageService
                .viewCount(0)
                .status(EDocumentStatus.PUBLIC)
                .course(courseRepository.findByCourseNameAndUniversityName(nameCourse, nameUniversity)
                        .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại")))
                .build();

        documentRepository.save(document);

        // Kiểm tra số lượt tải lên để cấp lượt tải miễn phí
        int uploadCount = documentRepository.countDocumentUploadInDay(LocalDate.now(), memberId);
        if (uploadCount == 1 || uploadCount == 3 || uploadCount % 5 == 0) {
            member.setDownloadLimit(member.getDownloadLimit() + 1);
            userRepository.save(member);
            return "Bạn đã tải lên đủ " + uploadCount + " tài liệu! Hệ thống đã cộng thêm 1 lượt tải miễn phí.";
        }

        return "Tài liệu đã được tải lên thành công!";
    }

    @Override
    public ResponseEntity<Resource> downloadDocument(UUID memberId, UUID documentId) {
        // Kiểm tra xem tài liệu có tồn tại không
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));

        // Kiểm tra xem người dùng có tồn tại không
        MemberEntity member = (MemberEntity) userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Nếu người dùng là Member thì kiểm tra số lượt tải
        if (member.getMembership().getLevel() == EMembershipLevel.FREE && member.getDownloadLimit() <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        // Nếu là Member, trừ đi 1 lượt tải
        if (member.getMembership().getLevel() == EMembershipLevel.FREE) {
            member.setDownloadLimit(member.getDownloadLimit() - 1);
            userRepository.save(member);
        }

        // Cập nhật số lượt tải xuống của tài liệu
        DocumentViewEntity documentView = DocumentViewEntity.builder()
                .document(document)
                .member(member)
                .downloadedAt(java.time.LocalDateTime.now())
                .build();

        documentViewRepository.save(documentView);

        // Lấy file từ server
        String fileName = document.getFileUrl().replace("/static/uploads/documents/", "");
        Path filePath = fileStorageService.getFilePath(fileName);
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new RuntimeException("Tài liệu không tồn tại trong hệ thống.");
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        // Trả file về client
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @Override
    public List<DocumentEntity> getRelatedDocuments(UUID documentId) {
        // Dùng Specification để tìm tài liệu liên quan
        Specification<DocumentEntity> spec = DocumentSpecification.relatedDocuments(documentId);
        return documentRepository.findAll(spec);
    }

    @Override
    public List<DocumentEntity> getRecommendedDocuments(UUID memberId) {
        // Kiểm tra người dùng có tồn tại không
        MemberEntity member = (MemberEntity) userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Lấy danh sách khóa học mà người dùng đang theo dõi
        List<UUID> courseIds = courseRepository.findFollowedCoursesByMemberId(memberId);

        // Nếu không theo dõi khóa học nào, lấy danh sách tài liệu đã xem
        List<UUID> viewedDocumentIds = documentViewRepository.findViewedDocumentsByMemberId(memberId);

        return documentRepository.findAll(DocumentSpecification.recommendDocuments(courseIds, viewedDocumentIds));
    }
}
