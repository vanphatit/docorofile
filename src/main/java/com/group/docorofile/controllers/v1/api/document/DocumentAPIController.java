package com.group.docorofile.controllers.v1.api.document;

import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.enums.EDocumentStatus;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.response.*;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.iDocumentService;
import com.group.docorofile.services.iUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/documents")
public class DocumentAPIController {
    @Autowired
    private iDocumentService documentService;

    @Autowired
    private iUserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/list")
    public Object getAllDocuments(@CookieValue(value = "jwtToken", required = false) String token) {
        try {
            if (jwtTokenUtil.getRoleFromToken(token).equals("ROLE_ADMIN") || jwtTokenUtil.getRoleFromToken(token).equals("ROLE_MODERATOR")) {
                return new SuccessResponse("Lấy danh sách tài liệu thành công!", 200, documentService.getAllAdminDocuments(), LocalDateTime.now());
            } else {
                return new SuccessResponse("Lấy danh sách tài liệu thành công!", 200, documentService.getAllUserDocuments(), LocalDateTime.now());
            }
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @GetMapping("/{documentId}")
    public Object viewDocumentById(@PathVariable UUID documentId,
                                   @CookieValue(value = "jwtToken", required = false) String token) {
        try {
            String userName = null;
            String role = null;
            if (token != null) {
                userName = jwtTokenUtil.getUsernameFromToken(token);
                role = jwtTokenUtil.getRoleFromToken(token);
            }
            return new SuccessResponse("Lấy thông tin tài liệu thành công!", 200, documentService.viewDocumentByIdForUI(documentId, role, userName), LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/upload")
    public Object uploadDocument(@RequestParam MultipartFile file,
                                 @RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam String nameCourse,
                                 @RequestParam String nameUniversity,
                                 @CookieValue(value = "jwtToken", required = false) String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UUID memberId = userService.findByEmail(username).get().getUserId();

            String response = documentService.uploadDocument(memberId, file, title, description, nameCourse, nameUniversity);
            return new CreatedResponse("Tài liệu đã được tải lên thành công!", response);
        } catch (RuntimeException e) {
            return new BadRequestError(e.getMessage());
        }
    }

    @GetMapping("/search")
    public Object searchDocuments(@RequestParam String keyword,
                                  @CookieValue(value = "jwtToken", required = false) String token) {
        try {
            if (token == null || token.isEmpty()) {
                return new UnauthorizedError("Bạn chưa đăng nhập!");
            }
            return documentService.searchDocuments(keyword, token);
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @GetMapping("/filter")
    public Object filterDocuments(@RequestParam(required = false) UUID courseId,
                                  @RequestParam(required = false) UUID universityId,
                                  @RequestParam(required = false) LocalDateTime uploadDate,
                                  @RequestParam(required = false) boolean sortByViews,
                                  @RequestParam(required = false) boolean sortByLikes,
                                  @RequestParam(required = false) boolean sortByDisLike,
                                  @RequestParam(required = false) String status,
                                  @CookieValue(value = "jwtToken", required = false) String token) {
        try {
            boolean isAdmin = false;
            if (token == null || token.isEmpty()) {
                return new UnauthorizedError("Bạn chưa đăng nhập!");
            } else if (jwtTokenUtil.getRoleFromToken(token).equals("ROLE_ADMIN")) {
                isAdmin = true;
            }
            return documentService.filterDocuments(courseId, universityId, uploadDate, sortByViews, sortByLikes, sortByDisLike, status, isAdmin);
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @GetMapping("/recommend")
    public Object getRecommendedDocuments(@CookieValue(value = "jwtToken", required = false) String token) {
        try {
            UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();

            List<DocumentEntity> recommendations = documentService.getRecommendedDocuments(memberId);
            if (recommendations.isEmpty()) {
                return new NotFoundError("Không có tài liệu nào để gợi ý.");
            }
            return new SuccessResponse("Gợi ý tài liệu thành công!", 200, recommendations, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/download/{documentId}")
    public Object downloadDocument(@PathVariable UUID documentId, @CookieValue(value = "jwtToken", required = false) String token) {
        try {
            UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();
            return documentService.downloadDocument(memberId, documentId);
        } catch (RuntimeException e) {
            return new ForbiddenError(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @DeleteMapping("/delete/{documentId}")
    public Object deleteDocument(@PathVariable UUID documentId,
                                 @CookieValue(value = "jwtToken", required = false) String token) {
        try {
            // Kiểm tra nếu token không tồn tại
            if (token == null || token.isEmpty()) {
                return new UnauthorizedError("Không có token! Vui lòng đăng nhập.");
            }

            // Kiểm tra vai trò (chỉ ADMIN hoặc MODERATOR mới được xóa)
            if (jwtTokenUtil.getRoleFromToken(token) == null || (!jwtTokenUtil.getRoleFromToken(token).equals("ROLE_ADMIN") && !jwtTokenUtil.getRoleFromToken(token).equals("ROLE_MODERATOR"))) {
                return new ForbiddenError("Bạn không có quyền xóa tài liệu này!");
            }

            // Nếu là MODERATOR thì kiểm tra xem ta liệu có số lượng report >= 3 không
            if (jwtTokenUtil.getRoleFromToken(token).equals("ROLE_MODERATOR")) {
                if (!jwtTokenUtil.getIsReportManageFromToken(token)) {
                    return new ForbiddenError("Bạn không có quyền xóa tài liệu này!");
                }
            }

            DocumentEntity document = documentService.getDocument(documentId);

            if (document == null) {
                return new NotFoundError("Không tìm thấy tài liệu này!");
            } else if (document.getReportCount() <= 3 && jwtTokenUtil.getRoleFromToken(token).equals("ROLE_MODERATOR")) {
                return new ForbiddenError("Tài liệu này chưa đủ số lượng report để xóa!");
            }

            boolean deleted = documentService.softDeleteDocument(documentId);
            if (deleted) {
                return new SuccessResponse("Tài liệu đã được xóa!", 200, null, LocalDateTime.now());
            } else {
                return new ConflictError("Tài liệu này đã bị xóa trước đó!");
            }

        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/history")
    public Object getHistoryDocuments(@CookieValue(value = "jwtToken", required = false) String token) {
        try {
            UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();
            List<UserDocumentDTO> historyDocuments = documentService.getHistoryDocumentsForUI(memberId);
            if (historyDocuments.isEmpty()) {
                return new NotFoundError("Không có tài liệu nào trong lịch sử xem!");
            }
            return new SuccessResponse("Lấy lịch sử xem tài liệu thành công!", 200, historyDocuments, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @GetMapping("/related/{documentId}")
    public Object getRelatedDocuments(@PathVariable UUID documentId) {
        try {
            List<DocumentEntity> relatedDocuments = documentService.getRelatedDocuments(documentId);
            if (relatedDocuments.isEmpty()) {
                return new NotFoundError("Không có tài liệu nào liên quan!");
            }
            return new SuccessResponse("Lấy danh sách tài liệu liên quan thành công!", 200, relatedDocuments, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/documentsByCourseFollowed")
    public Object getDocumentsByCourseFollowed (@CookieValue(value = "jwtToken", required = false) String token) {
        try {
            UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();
            boolean courseFollowed = userService.courseFollowedByMember(memberId);
            if (!courseFollowed) {
                return new NotFoundError("Bạn chưa theo dõi khóa học nào!");
            }
            List<UserDocumentDTO> documents = documentService.getDocumentByCourseAndFollowedByMemberForUI(memberId);
            if (documents.isEmpty()) {
                return new NotFoundError("Không có tài liệu nào trong khóa học đã theo dõi!");
            }
            return new SuccessResponse("Lấy danh sách tài liệu theo khóa học đã theo dõi thành công!", 200, documents, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/getDocumentsUpload")
    public Object getDocumentsUpload(@CookieValue(value = "jwtToken", required = false) String token) {
        try {
            UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();
            List<UserDocumentDTO> documents = documentService.getDocumentByAuthor(memberId);
            if (documents.isEmpty()) {
                return new NotFoundError("Không có tài liệu nào đã tải lên!");
            }
            return new SuccessResponse("Lấy danh sách tài liệu đã tải lên thành công!", 200, documents, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }
}
