package com.group.docorofile.controllers.v1.api.user;

import com.group.docorofile.entities.AdminEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.ModeratorEntity;
import com.group.docorofile.models.dto.DataTableResponse;
import com.group.docorofile.models.dto.UserInfoDTO;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.response.CreatedResponse;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.NotFoundError;
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
import java.util.*;

@RestController
@RequestMapping("/v1/api/users")
public class UserInfoAPIController {

    @Autowired
    private UserServiceImpl userService;

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
    public ResponseEntity<DataTableResponse<UserInfoDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "1") int draw
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserEntity> usersPage = userService.getAllUsers(pageable);

        List<UserInfoDTO> userInfoList = new ArrayList<>();
        for(UserEntity user : usersPage.getContent()) {
            UserInfoDTO userInfo = new UserInfoDTO();
            userInfo.setId(user.getUserId().toString());
            userInfo.setName(user.getFullName());
            userInfo.setEmail(user.getEmail());
            userInfo.setCurrent_plan("");
            if(user instanceof MemberEntity){
                userInfo.setRole("Member");
                userInfo.setCurrent_plan(((MemberEntity) user).getMembership().getLevel().name());
            }
            else if(user instanceof ModeratorEntity)
                userInfo.setRole("Moderator");
            else if(user instanceof AdminEntity)
                userInfo.setRole("Admin");

            userInfo.setStatus(user.isActive() ? "Active" : "Inactive" );
            userInfoList.add(userInfo);
        }

        DataTableResponse<UserInfoDTO> response = new DataTableResponse<>(
                draw,
                usersPage.getTotalElements(),
                usersPage.getTotalElements(),
                userInfoList
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
}