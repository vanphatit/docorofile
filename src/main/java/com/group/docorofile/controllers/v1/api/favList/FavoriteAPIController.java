package com.group.docorofile.controllers.v1.api.favList;

import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.models.dto.UserDocumentDTO;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.iUserService;
import com.group.docorofile.services.impl.FavoriteListServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/favorites")
public class FavoriteAPIController {

    @Autowired
    private FavoriteListServiceImpl favoriteListService;

    @Autowired
    private iUserService userService;

    @Autowired
    private JwtTokenUtil jwtUtils;

    @PreAuthorize( "hasRole('ROLE_MEMBER')" )
    @PostMapping("/add")
    public Object addToFavorites(@RequestParam UUID documentId,
                                 @CookieValue(value = "JWT", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String userName = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(userName).get().getUserId();
        if (userId == null) {
            return new UnauthorizedError("Token không hợp lệ!");
        }

        String message = favoriteListService.addToFavorites(userId, documentId);
        return new SuccessResponse(message, 200, null, LocalDateTime.now());
    }

    @PreAuthorize( "hasRole('ROLE_MEMBER')" )
    @DeleteMapping("/remove")
    public Object removeFromFavorites(@RequestParam UUID documentId,
                                      @CookieValue(value = "JWT", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String userName = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(userName).get().getUserId();
        if (userId == null) {
            return new UnauthorizedError("Token không hợp lệ!");
        }

        String message = favoriteListService.removeFromFavorites(userId, documentId);
        return new SuccessResponse(message, 200, null, LocalDateTime.now());
    }

    @PreAuthorize( "hasRole('ROLE_MEMBER')" )
    @GetMapping("/check")
    public Object isFavorited(@RequestParam UUID documentId,
                              @CookieValue(value = "JWT", required = false) String token) {
        if (token == null || token.isEmpty()) return new UnauthorizedError("Bạn chưa đăng nhập!");
        String username = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(username).get().getUserId();
        boolean isFav = favoriteListService.isFavorited(userId, documentId);
        return new SuccessResponse("Kiểm tra thành công", 200, isFav, LocalDateTime.now());
    }

    @PreAuthorize( "hasRole('ROLE_MEMBER')" )
    @GetMapping("/list")
    public Object getFavorites(@CookieValue(value = "JWT", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return new UnauthorizedError("Bạn chưa đăng nhập!");
        }

        String userName = jwtUtils.getUsernameFromToken(token);
        UUID userId = userService.findByEmail(userName).get().getUserId();
        if (userId == null) {
            return new UnauthorizedError("Token không hợp lệ!");
        }

        List<UserDocumentDTO> favoriteDocuments = favoriteListService.getFavorites(userId);

        if (favoriteDocuments.isEmpty()) {
            return new NotFoundError("Danh sách yêu thích trống!");
        }

        return new SuccessResponse("Danh sách tài liệu yêu thích!", 200, favoriteDocuments, LocalDateTime.now());
    }
}

