package com.group.docorofile.controllers.v1.api.comment;

import com.group.docorofile.response.BadRequestError;
import com.group.docorofile.response.ForbiddenError;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.iUserService;
import com.group.docorofile.services.impl.CommentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/comments")
public class CommentAPIController {

    @Autowired
    private CommentServiceImpl commentService;

    @Autowired
    private iUserService userService;

    @Autowired
    private JwtTokenUtil jwtUtils;

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/add")
    public Object addComment(@RequestParam UUID documentId,
                             @RequestParam String content,
                             @RequestParam(required = false) UUID parentCommentId,
                             @CookieValue(value = "JWT", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String userName = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(userName).get().getUserId();
        if (userId == null) {
            return new UnauthorizedError("Token không hợp lệ!");
        }

        try {
            String message = commentService.addComment(userId, documentId, content, parentCommentId);
            return new SuccessResponse(message, 200, null, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new BadRequestError(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{commentId}")
    public Object deleteComment(@PathVariable UUID commentId,
                                @CookieValue(value = "JWT", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String userName = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(userName).get().getUserId();
        if (userId == null) {
            return new UnauthorizedError("Token không hợp lệ!");
        }

        try {
            String message = commentService.deleteComment(commentId, userId);
            return new SuccessResponse(message, 200, null, LocalDateTime.now());
        } catch (RuntimeException e) {
            return new ForbiddenError(e.getMessage());
        }
    }

    @GetMapping("/{documentId}")
    public Object getComments(@PathVariable UUID documentId) {
        try {
            return new SuccessResponse("Lấy danh sách comment thành công!", 200, commentService.getCommentsByDocumentTree(documentId), LocalDateTime.now());
        } catch (RuntimeException e) {
            return new BadRequestError(e.getMessage());
        }
    }
}
