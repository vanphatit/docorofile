package com.group.docorofile.controllers.v1.api.user;

import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.response.CreatedResponse;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/users")
public class UserInfoAPIController {

    @Autowired
    private UserServiceImpl userService;

    // Tạo user mới, trả về CreatedResponse (status 201)
    @PostMapping("/newMember")
    public ResponseEntity<SuccessResponse> createUser(@RequestBody CreateUserRequest request) {
        UserEntity user = userService.createMember(request);
        CreatedResponse response = new CreatedResponse("User created successfully", user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/newManager")
    public ResponseEntity<SuccessResponse> createManager(@RequestBody CreateUserRequest request) {
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

    // Lấy danh sách tất cả user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse> getAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        SuccessResponse response = new SuccessResponse("Users retrieved successfully", HttpStatus.OK.value(), users, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // Cập nhật user theo ID
    @PreAuthorize("hasRole('ROLE_ADMIN)')")
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateUser(@PathVariable UUID id, @RequestBody CreateUserRequest request) {
        UserEntity user = userService.updateUser(id, request);
        SuccessResponse response = new SuccessResponse("User updated successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

//    @PreAuthorize("hasRole('ROLE_MEMBER')")
//    @PutMapping("/updateMyProfile")
//    public ResponseEntity<SuccessResponse> updateMyProfile(@PathVariable UUID id, @RequestBody CreateUserRequest request) {
//        UserEntity user = userService.updateUser(id, request);
//        SuccessResponse response = new SuccessResponse("User updated successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
//        return ResponseEntity.ok(response);
//    }

    // Xóa user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deactivateUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        SuccessResponse response = new SuccessResponse("User deleted successfully", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}