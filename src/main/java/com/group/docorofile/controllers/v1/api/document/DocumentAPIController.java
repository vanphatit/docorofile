package com.group.docorofile.controllers.v1.api.document;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.EDocumentStatus;
import com.group.docorofile.models.dto.UploadResultDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.request.DocumentMetadataRequest;
import com.group.docorofile.response.*;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.iDocumentService;
import com.group.docorofile.services.iFavoriteListService;
import com.group.docorofile.services.iUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Member;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/v1/api/documents")
public class DocumentAPIController {
    @Autowired
    private iDocumentService documentService;

    @Autowired
    private iUserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private iFavoriteListService favoriteListService;

    @GetMapping("/list")
    public Object getAllDocuments(@CookieValue(value = "JWT", required = false) String token) {
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

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/upload")
    public Object uploadDocument(@RequestParam MultipartFile file,
                                 @RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam String nameCourse,
                                 @RequestParam String nameUniversity,
                                 @CookieValue(value = "JWT", required = false) String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UUID memberId = userService.findByEmail(username).get().getUserId();

            String response = documentService.uploadDocument(memberId, file, title, description, nameCourse, nameUniversity);
            return new CreatedResponse("Tài liệu đã được tải lên thành công!", response);
        } catch (RuntimeException e) {
            return new BadRequestError(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/upload-files")
    public Object uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                      @CookieValue(value = "JWT", required = false) String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UUID memberId = userService.findByEmail(username).get().getUserId();

            List<UUID> documentIds = new ArrayList<>();
            for (MultipartFile file : files) {
                UUID docId = documentService.saveFileOnly(memberId, file);
                documentIds.add(docId);
            }

            return new CreatedResponse("Tải lên thành công", documentIds);
        } catch (RuntimeException e) {
            return new BadRequestError(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/upload-metadata")
    public Object updateDocumentMetadata(@RequestBody List<DocumentMetadataRequest> metadataList,
                                         @CookieValue(value = "JWT", required = false) String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UUID memberId = userService.findByEmail(username).get().getUserId();
            if (memberId == null) {
                return new UnauthorizedError("Bạn chưa đăng nhập!");
            }
            if (metadataList.isEmpty()) {
                return new BadRequestError("Danh sách tài liệu rỗng!");
            }

            MemberEntity member = (MemberEntity) userService.findById(memberId).get();

            int downloadLimit = member.getDownloadLimit();

            List<UserDocumentDTO> documents = new ArrayList<>();

            for (DocumentMetadataRequest req : metadataList) {
                UUID documentId = UUID.fromString(req.getDocumentId());
               documents.add(documentService.updateMetadata(documentId, req.getTitle(), req.getDescription(),
                        req.getNameCourse(), req.getNameUniversity())
               );
            }

            int finalDownloadLimit = member.getDownloadLimit();
            int addedDownloadLimit = finalDownloadLimit - downloadLimit;

            UploadResultDTO response = new UploadResultDTO();
            response.setDocuments(documents);
            response.setAddedTurns(addedDownloadLimit);
            response.setTotalDownloadLimit(finalDownloadLimit);

            return new SuccessResponse("Thay đổi thông tin tài liệu thành công!", 200, response, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new BadRequestError(e.getMessage());
        }
    }

    @GetMapping("/metadata")
    public Object getMetaData() {
        try {
            List<CourseEntity> courses = documentService.findAllWithUniversity();
            Map<String, List<String>> result = new HashMap<>();
            for (CourseEntity course : courses) {
                String univName = course.getUniversity().getUnivName();
                result.computeIfAbsent(univName, k -> new ArrayList<>()).add(course.getCourseName());
            }
            return new SuccessResponse("Lấy thông tin metadata thành công!", 200, result, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @GetMapping("/search")
    public Object searchDocuments(@RequestParam String keyword,
                                  @CookieValue(value = "JWT", required = false) String token) {
        try {
            if (token == null || token.isEmpty()) {
                return new UnauthorizedError("Bạn chưa đăng nhập!");
            }
            return documentService.searchDocuments(keyword, token);
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @GetMapping("/search/suggestions")
    public Object getSearchSuggestions(@RequestParam String keyword) {
        try {
            Page<UserDocumentDTO> suggestions = documentService.getSearchSuggestions(keyword);
            return new SuccessResponse("Gợi ý tìm kiếm thành công!", 200, suggestions, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError("Lỗi khi tìm kiếm gợi ý: " + e.getMessage());
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
                                  @CookieValue(value = "JWT", required = false) String token) {
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

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/recommend")
    public Object getRecommendedDocuments( @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "6") int size,
                                          @CookieValue(value = "JWT", required = false) String token) {
        try {
            UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();

            Page<UserDocumentDTO> recommendations = documentService.getRecommendedDocumentsForUI(memberId, page, size);
            if (recommendations.isEmpty()) {
                return new NotFoundError("Không có tài liệu nào để gợi ý.");
            }
            return new SuccessResponse("Gợi ý tài liệu thành công!", 200, recommendations, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @GetMapping("/download/{documentId}")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID documentId,
                                                     @CookieValue(value = "JWT", required = false) String token) {
        UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"))
                .getUserId();
        return documentService.downloadDocument(memberId, documentId);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @DeleteMapping("/delete/{documentId}")
    public Object deleteDocument(@PathVariable UUID documentId,
                                 @CookieValue(value = "JWT", required = false) String token) {
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

    @GetMapping("/history")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public Object getHistoryDocuments( @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "6") int size,
                                       @CookieValue(value = "JWT", required = false) String token)
    {
        try {
            String email = jwtTokenUtil.getUsernameFromToken(token);
            Optional<UserEntity> optionalUser = userService.findByEmail(email);

            if (optionalUser.isEmpty()) {
                return new NotFoundError("Không tìm thấy người dùng với email: " + email);
            }

            UUID memberId = optionalUser.get().getUserId();
            Page<UserDocumentDTO> historyDocuments = documentService.getHistoryDocumentsForUI(memberId, page, size);

            if (historyDocuments.isEmpty()) {
                return new NotFoundError("Không có tài liệu nào trong lịch sử xem!");
            }

            return new SuccessResponse("Lấy lịch sử xem tài liệu thành công!", 200, historyDocuments, LocalDateTime.now());
        } catch (Exception e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @GetMapping("/related/{documentId}")
    public Object getRelatedDocumentsPaginated(
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        try {
            Page<UserDocumentDTO> relatedDocuments = documentService.getRelatedDocumentsForUI(documentId, page, size);
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
    public Object getDocumentsByCourseFollowed (@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "6") int size,
                                                @CookieValue(value = "JWT", required = false) String token) {
        try {
            UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();
            if (memberId == null) {
                return new UnauthorizedError("Bạn chưa đăng nhập!");
            }
            System.out.println("===================== " + memberId);
            boolean courseFollowed = userService.courseFollowedByMember(memberId);

            if (!courseFollowed) {
                return new NotFoundError("Bạn chưa theo dõi khóa học nào!");
            }
            Page<UserDocumentDTO> documents = documentService.getDocumentByCourseAndFollowedByMemberForUI(memberId, page, size);
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
    public Object getDocumentsUpload(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "6") int size,
                                     @CookieValue(value = "JWT", required = false) String token) {
        try {
            UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();
            Page<UserDocumentDTO> documents = documentService.getDocumentByAuthor(memberId, page, size);
            if (documents.isEmpty()) {
                return new NotFoundError("Không có tài liệu nào đã tải lên!");
            }
            return new SuccessResponse("Lấy danh sách tài liệu đã tải lên thành công!", 200, documents, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new InternalServerError(e.getMessage());
        }
    }

    @PreAuthorize( "hasRole('ROLE_MEMBER')" )
    @GetMapping("/fav_list")
    public Object getFavorites(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "6") int size,
                               @CookieValue(value = "JWT", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        UUID memberId = userService.findByEmail(jwtTokenUtil.getUsernameFromToken(token)).get().getUserId();
        if (memberId == null) {
            return new UnauthorizedError("Token không hợp lệ!");
        }

        Page<UserDocumentDTO> favoriteDocuments = favoriteListService.getFavoritesUI(memberId, page, size);

        if (favoriteDocuments.isEmpty()) {
            return new NotFoundError("Danh sách yêu thích trống!");
        }

        return new SuccessResponse("Danh sách tài liệu yêu thích!", 200, favoriteDocuments, LocalDateTime.now());
    }

    @GetMapping("/view/{documentId}")
    public Object viewDocumentById(@PathVariable UUID documentId,
                                   @CookieValue(value = "JWT", required = false) String token) {
        try {
            String userName = null;
            String role = null;
            if (token != null) {
                userName = jwtTokenUtil.getUsernameFromToken(token);
                role = jwtTokenUtil.getRoleFromToken(token);
            }
            Object dto = documentService.viewDocumentByIdForUI(documentId, role, userName);
            return new SuccessResponse("Lấy thông tin tài liệu thành công!", 200, dto, LocalDateTime.now());
        } catch (NotFoundError e) {
            return new NotFoundError("Không tìm thấy tài liệu này!");
        } catch (UnauthorizedError e) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        } catch (Exception e) {
            return new InternalServerError(e.getMessage());
        }
    }
}
