package com.group.docorofile.controllers.v1.api.followCourse;
import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.enums.EDocumentStatus;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.models.course.CourseDetailDTO;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.models.mappers.DocumentMapper;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.iCourseService;
import com.group.docorofile.services.iDocumentService;
import com.group.docorofile.services.iFollowCourseService;
import com.group.docorofile.services.iUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api")
public class FollowCourseAPIController {

    @Autowired
    private iFollowCourseService followCourseService;

    @Autowired
    private iUserService userService;

    @Autowired
    private iDocumentService documentService;

    @Autowired
    private JwtTokenUtil jwtUtils;

    @Autowired
    private iCourseService courseService;

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @PostMapping("/follow")
    public Object followCourse(@RequestParam UUID courseId,
                               @CookieValue(value = "JWT", required = false) String token) {

        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String userEmail = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Token không hợp lệ hoặc người dùng không tồn tại."))
                .getUserId();

        followCourseService.followCourse(userId, courseId);

        return new SuccessResponse(
                "Bạn đã theo dõi khóa học thành công!",
                200,
                null,
                LocalDateTime.now()
        );
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @DeleteMapping("/unfollow")
    public Object unfollowCourse(@RequestParam UUID courseId,
                                 @CookieValue(value = "JWT", required = false) String token) {

        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String userEmail = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Token không hợp lệ hoặc người dùng không tồn tại."))
                .getUserId();

        followCourseService.unfollowCourse(userId, courseId);

        return new SuccessResponse(
                "Đã bỏ theo dõi khóa học thành công.",
                200,
                null,
                LocalDateTime.now()
        );
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @GetMapping("/followed")
    public Object getFollowedCourses(@CookieValue(value = "JWT", required = false) String token) {

        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String userEmail = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Token không hợp lệ hoặc người dùng không tồn tại."))
                .getUserId();

        List<CourseCreatedResponseDTO> followed = followCourseService.getFollowedCourses(userId);

        return new SuccessResponse(
                "Lấy danh sách khóa học đang theo dõi thành công!",
                200,
                followed,
                LocalDateTime.now()
        );
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/course/documents")
    @ResponseBody
    public Object getDocumentsByCourseId(@RequestParam UUID courseId) {
        List<DocumentEntity> documents = documentService.getDocumentByCourseId(courseId);
        documents.removeIf(document->document.getStatus().equals(EDocumentStatus.DRAFT)||document.getStatus().equals(EDocumentStatus.DELETED));
        List<UserDocumentDTO> dtos = documents.stream().map(DocumentMapper::toUserDTO).toList();
        return new SuccessResponse("Lấy tài liệu theo khóa học thành công", 200, dtos, LocalDateTime.now());
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/course/detail")
    public Object getCourseDetail(@RequestParam UUID courseId) {
        CourseEntity course = courseService.findById(courseId).orElse(null);
        if (course == null) return new NotFoundError("Không tìm thấy khóa học");

        CourseDetailDTO dto = new CourseDetailDTO();
        dto.setCourseId(courseId);
        dto.setCourseName(course.getCourseName());
        dto.setUniversityName(course.getUniversity().getUnivName());
        List<DocumentEntity> list = documentService.getDocumentByCourseId(courseId);
        dto.setTotalDocuments(list.size());
        dto.setTotalFollowers(followCourseService.countFollowerByCourse(courseId));
        return new SuccessResponse("OK", 200, dto, LocalDateTime.now());
    }


}