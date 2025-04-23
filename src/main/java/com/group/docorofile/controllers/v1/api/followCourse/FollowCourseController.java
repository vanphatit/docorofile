package com.group.docorofile.controllers.v1.api.followCourse;
import com.group.docorofile.exceptions.UserNotFoundException;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.impl.FollowCourseServiceImpl;
import com.group.docorofile.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api")
public class FollowCourseController {

    @Autowired
    private FollowCourseServiceImpl followCourseService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private JwtTokenUtil jwtUtils;

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
}