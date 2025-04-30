package com.group.docorofile.controllers.v1.api.user;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.response.CreatedResponse;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.services.impl.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/users")
public class UserInfoAPIController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/newMember")
    public ResponseEntity<SuccessResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserEntity user = userService.createMember(request);
        CreatedResponse response = new CreatedResponse("User created successfully", user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/newManager")
    public ResponseEntity<SuccessResponse> createManager(@Valid @RequestBody CreateUserRequest request) {
        UserEntity user = userService.createManager(request);
        CreatedResponse response = new CreatedResponse("User created successfully", user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Lấy 1 user theo ID
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getUser(@PathVariable UUID id) {
        UserEntity user = userService.getUserById(id)
                .orElseThrow(() -> new NotFoundError("User not found"));
        SuccessResponse response = new SuccessResponse("User retrieved successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<UserEntity> usersPage = userService.getAllUsers(pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", usersPage.getContent());         // Danh sách user
        responseData.put("currentPage", usersPage.getNumber());      // Chỉ số trang hiện tại (0-based)
        responseData.put("totalItems", usersPage.getTotalElements());
        responseData.put("totalPages", usersPage.getTotalPages());

        SuccessResponse response = new SuccessResponse(
                "Users retrieved successfully",
                HttpStatus.OK.value(),
                responseData,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

//    // Cập nhật user theo ID
//    @PreAuthorize("hasRole('ROLE_ADMIN)')")
//    @PutMapping("/{id}")
//    public ResponseEntity<SuccessResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody CreateUserRequest request) {
//        UserEntity user = userService.updateUser(id, request);
//        SuccessResponse response = new SuccessResponse("User updated successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
//        return ResponseEntity.ok(response);
//    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PutMapping("/updateMyProfile")
    public ResponseEntity<SuccessResponse> updateMyProfile(@PathVariable UUID id, @RequestBody UpdateProfileRequest request) {
        UserEntity user = userService.updateMyProfile(id, request);
        SuccessResponse response = new SuccessResponse("User updated successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // Huỷ kích hoạt user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deactivateUser(@PathVariable UUID id) {
        UserEntity user = userService.getUserById(id)
                .orElseThrow(() -> new NotFoundError("User not found"));
        userService.deactivateUser(id);
        SuccessResponse response = new SuccessResponse("User deleted successfully", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download-turn-info")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ResponseEntity<SuccessResponse> downloadTurnInfo(
            @CookieValue(value = "JWT", required = false) String token) {
        try {
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new SuccessResponse("Bạn chưa đăng nhập!", HttpStatus.UNAUTHORIZED.value(), null, LocalDateTime.now()));
            }


            String username = jwtTokenUtil.getUsernameFromToken(token);
            UUID memberId = userService.findByEmail(username).orElseThrow().getUserId();
            MemberEntity member = (MemberEntity) userService.findById(memberId).orElseThrow();

            return ResponseEntity.ok(
                    new SuccessResponse("Turn info downloaded successfully", HttpStatus.OK.value(), member.getDownloadLimit(), LocalDateTime.now())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SuccessResponse("Error downloading turn info", HttpStatus.INTERNAL_SERVER_ERROR.value(), null, LocalDateTime.now()));
        }
    }

}