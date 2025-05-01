package com.group.docorofile.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.docorofile.entities.*;
import com.group.docorofile.enums.EDocumentStatus;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.models.dto.AdminDocumentDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.models.mappers.DocumentMapper;
import com.group.docorofile.repositories.*;
import com.group.docorofile.response.InternalServerError;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.services.iDocumentService;
import com.group.docorofile.services.specifications.DocumentSpecification;
import com.group.docorofile.utils.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
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

    @Value("${document.upload.dir}")
    private String uploadDir;

    @Override
    public DocumentEntity createDocument(DocumentEntity document) {
        try {
            document.setUploadedDate(LocalDateTime.now());
            return documentRepository.save(document);
        } catch (RuntimeException e) {
            throw new InternalServerError("Lỗi khi tạo tài liệu!");
        }
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
    public Page<UserDocumentDTO> getAllUserDocuments(int page, int size) {
        List <UserDocumentDTO> documents = documentRepository.findAll().stream().map(DocumentMapper::toUserDTO).toList();
        int start = Math.min(page * size, documents.size());
        int end = Math.min(start + size, documents.size());
        List<UserDocumentDTO> pagedList = documents.subList(start, end);
        return new PageImpl<>(pagedList, PageRequest.of(page, size), documents.size());
    }

    @Override
    public Page<AdminDocumentDTO> getAllAdminDocuments( int page, int size) {
        List<AdminDocumentDTO> documents = documentRepository.findAll().stream().map(DocumentMapper::toAdminDTO).toList();
        int start = Math.min(page * size, documents.size());
        int end = Math.min(start + size, documents.size());
        List<AdminDocumentDTO> pagedList = documents.subList(start, end);
        return new PageImpl<>(pagedList, PageRequest.of(page, size), documents.size());
    }

    @Override
    public Object viewDocumentByIdForUI(UUID documentId, String role, String userName) {
        // Kiểm tra tài liệu có tồn tại không
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundError("Tài liệu không tồn tại!"));

        try {
            // Tăng viewCount
            document.setViewCount(document.getViewCount() + 1);
            documentRepository.save(document);
            if (role.contains("ROLE_MEMBER")) {
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

                    documentViewRepository.save(view);
                }
            }
            // Kiểm tra vai trò từ token
            if (role.contains("ROLE_ADMIN") || role.contains("ROLE_MODERATOR")) {
                return DocumentMapper.toAdminDTO(document);
            } else {
                return DocumentMapper.toUserDTO(document);
            }
        } catch (RuntimeException e) {
            return new UnauthorizedError(e.getMessage());
        }
    }

    @Override
    public Page<UserDocumentDTO> searchDocumentsForGuest(String keyword, int page, int size) {
        List<DocumentEntity> documents = documentRepository.findAll(
                DocumentSpecification.searchByKeyword(keyword)
        );

        if (documents.isEmpty()) {
            throw new NotFoundError("Không tìm thấy tài liệu nào!");
        }

        // Chỉ trả về các tài liệu đã duyệt/public (ví dụ: status = APPROVED)
        List<UserDocumentDTO> filtered = documents.stream()
                .filter(doc -> doc.getStatus().equals("APPROVED"))
                .map(DocumentMapper::toUserDTO)
                .toList();

        int start = Math.min(page * size, filtered.size());
        int end = Math.min(start + size, filtered.size());
        List<UserDocumentDTO> paged = filtered.subList(start, end);

        return new PageImpl<>(paged, PageRequest.of(page, size), filtered.size());
    }

    @Override
    public Object searchDocumentsForAuthUser(String keyword, int page, int size, String role) {
        try {
            List<DocumentEntity> documents = documentRepository.findAll(
                    DocumentSpecification.searchByKeyword(keyword)
            );

            if (documents.isEmpty()) {
                return new NotFoundError("Không tìm thấy tài liệu nào!");
            }

            if (role.contains("ROLE_ADMIN") || role.contains("ROLE_MODERATOR")) {
                List<AdminDocumentDTO> adminDocs = documents.stream()
                        .map(DocumentMapper::toAdminDTO)
                        .toList();
                int start = Math.min(page * size, adminDocs.size());
                int end = Math.min(start + size, adminDocs.size());
                return new PageImpl<>(adminDocs.subList(start, end), PageRequest.of(page, size), adminDocs.size());
            } else {
                List<UserDocumentDTO> userDocs = documents.stream()
                        .map(DocumentMapper::toUserDTO)
                        .toList();
                int start = Math.min(page * size, userDocs.size());
                int end = Math.min(start + size, userDocs.size());
                return new PageImpl<>(userDocs.subList(start, end), PageRequest.of(page, size), userDocs.size());
            }

        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @Override
    public Page<UserDocumentDTO> getSearchSuggestions(String keyword) {
        Pageable limitFive = PageRequest.of(0, 5); // Giới hạn top 5 gợi ý

        Page<DocumentEntity> documentPage = documentRepository
                .findByTitleContainingIgnoreCase(keyword, limitFive);

        if (documentPage.isEmpty()) {
            throw new NotFoundError("Không có gợi ý phù hợp!");
        }

        return documentPage.map(DocumentMapper::toUserDTO);
    }

    @Override
    public Object filterDocuments(String keyword, UUID courseId, UUID universityId, String uploadDate,
                                  boolean sortByViews, boolean sortByLikes, boolean sortByDisLike,
                                  boolean sortByNewest, boolean sortByOldest, boolean sortByReportCount,
                                  String status, boolean isAdmin, int page, int size) {
        try {
            List<DocumentEntity> searchDocuments = documentRepository.findAll(
                    DocumentSpecification.searchByKeyword(keyword)
            );
            // covert String uploadDate to LocalDateTime
            LocalDateTime uploadDateTime = null;
            if (uploadDate != null && !uploadDate.isEmpty()) {
                uploadDateTime = LocalDateTime.parse(uploadDate);
            }
            List<DocumentEntity> documents = documentRepository.findAll(DocumentSpecification.filterDocuments(searchDocuments, courseId, universityId, uploadDateTime, sortByViews, sortByLikes, sortByDisLike, sortByNewest, sortByOldest, sortByReportCount, status, isAdmin));
            if (isAdmin) {
                List<AdminDocumentDTO> adDocDTO = documents.stream().map(DocumentMapper::toAdminDTO).toList();
                int start = Math.min(page * size, adDocDTO.size());
                int end = Math.min(start + size, adDocDTO.size());
                List<AdminDocumentDTO> pagedList = adDocDTO.subList(start, end);
                return new PageImpl<>(pagedList, PageRequest.of(page, size), adDocDTO.size());
            } else {
                List<UserDocumentDTO> userDocDTO = documents.stream().map(DocumentMapper::toUserDTO).toList();
                int start = Math.min(page * size, userDocDTO.size());
                int end = Math.min(start + size, userDocDTO.size());
                List<UserDocumentDTO> pagedList = userDocDTO.subList(start, end);
                return new PageImpl<>(pagedList, PageRequest.of(page, size), userDocDTO.size());
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
    public UUID saveFileOnly(UUID memberId, MultipartFile file) {
        // Kiểm tra user tồn tại
        MemberEntity member = (MemberEntity) userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        String fileUrl;
        try {
            fileUrl = fileStorageService.saveFile(file);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file");
        }

        // Tạo DocumentEntity chưa có metadata
        DocumentEntity document = DocumentEntity.builder()
                .documentId(UUID.randomUUID())
                .uploadedDate(LocalDateTime.now())
                .author(member)
                .fileUrl(fileUrl)
                .status(EDocumentStatus.DRAFT) // Trạng thái tạm
                .viewCount(0)
                .build();

        documentRepository.save(document);
        return document.getDocumentId();
    }

    @Override
    public UserDocumentDTO updateMetadata(UUID documentId, String title, String description,
                                          String nameCourse, String nameUniversity) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));

        if (!courseRepository.existsByCourseName(nameCourse)) {
            throw new RuntimeException("Không tìm thấy khóa học!");
        }

        if (!universityRepository.existsByUnivName(nameUniversity)) {
            throw new RuntimeException("Không tìm thấy trường học!");
        }

        if (documentRepository.existsByTitleAndAuthor_UserId(title, document.getAuthor().getUserId())) {
            throw new RuntimeException("Bạn đã tải lên tài liệu này trước đó.");
        }

        // Gán metadata
        document.setTitle(title);
        document.setDescription(description);
        document.setStatus(EDocumentStatus.PUBLIC);
        document.setCourse(courseRepository.findByCourseNameAndUniversityName(nameCourse, nameUniversity)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy course")));

        documentRepository.save(document);

        // Tính lượt upload và tăng downloadLimit nếu cần
        UUID memberId = document.getAuthor().getUserId();
        int uploadCount = documentRepository.countDocumentUploadInDay(LocalDate.now(), memberId);
        if (uploadCount == 1 || uploadCount == 3 || uploadCount % 5 == 0) {
            MemberEntity member = document.getAuthor();
            member.setDownloadLimit(member.getDownloadLimit() + 1);
            userRepository.save(member);
        }

        return DocumentMapper.toUserDTO(document);
    }

    public class JsonErrorHelper {
        public static ResponseEntity<Resource> error(int code, String message, boolean redirectToUpload) {
            Map<String, Object> body = new HashMap<>();
            body.put("code", code);
            body.put("message", message);
            body.put("redirectToUpload", redirectToUpload);

            try {
                byte[] json = new ObjectMapper().writeValueAsBytes(body);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ByteArrayResource(json));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Không thể chuyển lỗi sang JSON", e);
            }
        }
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
            return JsonErrorHelper.error(403,
                    "Bạn đã hết lượt tải. Vuic lòng tải lên tài liệu để nhận thêm lượt tải miễn phí.",
                    true);
        }

        // Nếu là Member, trừ đi 1 lượt tải
        if (member.getMembership().getLevel() == EMembershipLevel.FREE) {
            member.setDownloadLimit(member.getDownloadLimit() - 1);
            userRepository.save(member);
        }

        // Kiem tra xem nguoi dung da xem tai lieu chua
        boolean alreadyViewed = documentViewRepository.existsByDocument_DocumentIdAndMember_UserId(documentId, memberId);
        if (!alreadyViewed) {
            // Ghi lại lượt xem vào DocumentViewEntity
            DocumentViewEntity documentView = DocumentViewEntity.builder()
                    .document(document)
                    .member(member)
                    .viewedAt(java.time.LocalDateTime.now())
                    .downloadedAt(java.time.LocalDateTime.now())
                    .build();

            documentViewRepository.save(documentView);
        } else {
            // Cập nhật lượt tải
            DocumentViewEntity documentView = documentViewRepository.findByDocument_DocumentIdAndMember_UserId(documentId, memberId);
            documentView.setDownloadedAt(java.time.LocalDateTime.now());
            documentViewRepository.save(documentView);
        }

        String filePathStr = document.getFileUrl();
        String relativePath = filePathStr.replaceFirst("^.*?/uploads/documents/", "");
        Path filePath = Paths.get(uploadDir, relativePath);
        String fileName = filePath.getFileName().toString();

        Resource resource;
        try {
            if (!Files.exists(filePath)) {
                throw new NotFoundError("Tài liệu không tồn tại trong hệ thống (missing file).");
            }

            resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundError("Tài liệu không tồn tại hoặc không thể đọc được.");
            }

        } catch (Exception e) {
            e.printStackTrace(); //
            throw new InternalServerError("Không thể load file: " + e.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @Override
    public Page<DocumentEntity> getRelatedDocuments(UUID documentId, int page, int size) {
        // Dùng Specification để tìm tài liệu liên quan
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại"));
        Specification<DocumentEntity> spec = DocumentSpecification.relatedDocuments(document);
        return documentRepository.findAll(spec, PageRequest.of(page, size));
    }

    @Override
    public Page<UserDocumentDTO> getRelatedDocumentsForUI(UUID documentId, int page, int size) {
        Page<DocumentEntity> relatedDocuments = getRelatedDocuments(documentId, page, size);
        return relatedDocuments.map(DocumentMapper::toUserDTO);
    }

    @Override
    public Page<DocumentEntity> getRecommendedDocuments(UUID memberId, int page, int size) {
        // Kiểm tra người dùng có tồn tại không
        MemberEntity member = (MemberEntity) userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Lấy danh sách khóa học mà người dùng đang theo dõi
        List<UUID> courseIds = courseRepository.findFollowedCoursesByMemberId(memberId);

        // Nếu không theo dõi khóa học nào, lấy danh sách tài liệu đã xem
        List<UUID> viewedDocumentIds = documentViewRepository.findViewedDocumentsByMemberId(memberId);

        Specification<DocumentEntity> spec = DocumentSpecification.recommendDocuments(courseIds, viewedDocumentIds);

        return documentRepository.findAll(spec, PageRequest.of(page, size));
    }

    @Override
    public Page<UserDocumentDTO> getRecommendedDocumentsForUI(UUID memberId, int page, int size) {
        Page<DocumentEntity> recommendedDocuments = getRecommendedDocuments(memberId, page, size);
        return recommendedDocuments.map(DocumentMapper::toUserDTO);
    }

    @Override
    public Page<DocumentEntity> getHistoryDocuments(UUID memberId, int page, int size) {
        try {
            MemberEntity member = (MemberEntity) userRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            List<DocumentViewEntity> historyViewDocuments = documentViewRepository.findHistoryDocumentsByMemberId(member.getUserId());
            List<DocumentEntity> allDocuments = historyViewDocuments.stream()
                    .map(DocumentViewEntity::getDocument)
                    .collect(Collectors.toList());

            int start = Math.min(page * size, allDocuments.size());
            int end = Math.min(start + size, allDocuments.size());
            List<DocumentEntity> pagedList = allDocuments.subList(start, end);

            return new PageImpl<>(pagedList, PageRequest.of(page, size), allDocuments.size());
        } catch (RuntimeException e) {
            throw new InternalServerError(e.getMessage());
        }
    }

    @Override
    public Page<UserDocumentDTO> getHistoryDocumentsForUI(UUID memberId, int page, int size) {
        try {
            Page<DocumentEntity> historyViewDocuments = getHistoryDocuments(memberId, page, size);
            return historyViewDocuments.map(DocumentMapper::toUserDTO);
        } catch (RuntimeException e) {
            throw new InternalServerError(e.getMessage());
        }
    }

    @Override
    public List<DocumentEntity> getDocumentByCourseId(UUID courseId) {
        return documentRepository.findByCourse_CourseId(courseId);
    }

    @Override
    public List<DocumentEntity> getDocumentByUniversityId(String univName) {
        return documentRepository.findByCourse_University_UnivName(univName);
    }

    @Override
    public List<DocumentEntity> getDocumentByUniversityAndCourse(String univName, String courseName) {
        return documentRepository.findByCourse_University_UnivNameAndCourse_CourseName(univName, courseName);
    }

    @Override
    public Page<DocumentEntity> getDocumentByCourseAndFollowedByMember(UUID memberId, int page, int size) {
        try {
            List<UUID> courseIds = courseRepository.findFollowedCoursesByMemberId(memberId);
            return documentRepository.findAll(DocumentSpecification.recommendDocuments(courseIds, null), PageRequest.of(page, size));
        } catch (RuntimeException e) {
            throw new InternalServerError(e.getMessage());
        }
    }

    @Override
    public Page<UserDocumentDTO> getDocumentByCourseAndFollowedByMemberForUI(UUID memberId, int page, int size) {
        try {
            Page<DocumentEntity> documents = getDocumentByCourseAndFollowedByMember(memberId, page, size);
            return documents.map(DocumentMapper::toUserDTO);
        } catch (RuntimeException e) {
            throw new InternalServerError(e.getMessage());
        }
    }

    @Override
    public Page<UserDocumentDTO> getDocumentByAuthor(UUID authorId, int page, int size) {
        try {
            List<DocumentEntity> documents = documentRepository.findByAuthor_UserId(authorId);
            List<UserDocumentDTO> dtos = documents.stream()
                    .map(DocumentMapper::toUserDTO)
                    .collect(Collectors.toList());

            int start = Math.min(page * size, dtos.size());
            int end = Math.min(start + size, dtos.size());
            List<UserDocumentDTO> pagedList = dtos.subList(start, end);

            return new PageImpl<>(pagedList, PageRequest.of(page, size), dtos.size());
        } catch (RuntimeException e) {
            throw new InternalServerError(e.getMessage());
        }
    }

    @Override
    public List<CourseEntity> findAllWithUniversity() {
        try {
            return courseRepository.findAllWithUniversity();
        } catch (RuntimeException e) {
            throw new InternalServerError(e.getMessage());
        }
    }
}
