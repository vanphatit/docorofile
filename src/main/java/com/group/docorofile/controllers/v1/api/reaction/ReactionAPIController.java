package com.group.docorofile.controllers.v1.api.reaction;

import com.group.docorofile.response.BadRequestError;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.iUserService;
import com.group.docorofile.services.impl.ReactionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/reactions")
public class ReactionAPIController {

    @Autowired
    private ReactionServiceImpl reactionService;

    @Autowired
    private iUserService userService;

    @Autowired
    private JwtTokenUtil jwtUtils;

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/update")
    public Object updateReaction(@RequestParam UUID documentId,
                                 @RequestParam boolean isLike,
                                 @RequestParam boolean isDislike,
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
            String message = reactionService.updateReact(userId, documentId, isLike, isDislike);
            return new SuccessResponse(message, 200, null, LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            return new BadRequestError(e.getMessage());
        } catch (RuntimeException e) {
            return new NotFoundError(e.getMessage());
        }
    }

    @GetMapping("/status")
    public Object getReactionStatus(@RequestParam UUID documentId,
                                    @CookieValue(value = "JWT", required = false) String token) {
        if (token == null || token.isEmpty())
            return new UnauthorizedError("Bạn chưa đăng nhập!");

        String username = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(username).get().getUserId();
        String status = reactionService.getUserReaction(userId, documentId); // "like" | "dislike" | null
        return new SuccessResponse("Lấy trạng thái thành công", 200, status, LocalDateTime.now());
    }

    @GetMapping("/count/{documentId}")
    public Object getReactionCount(@PathVariable UUID documentId) {
        int likeCount = reactionService.countLike(documentId);
        int dislikeCount = reactionService.countDislike(documentId);

        return new SuccessResponse("Thống kê phản ứng!", 200,
                Map.of("likeCount", likeCount, "dislikeCount", dislikeCount),
                LocalDateTime.now());
    }
}
