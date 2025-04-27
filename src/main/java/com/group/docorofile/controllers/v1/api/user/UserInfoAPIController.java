package com.group.docorofile.controllers.v1.api.user;

import com.group.docorofile.entities.AdminEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.ModeratorEntity;
import com.group.docorofile.models.dto.DataTableResponse;
import com.group.docorofile.models.dto.UserInfoDTO;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.response.CreatedResponse;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.services.impl.UserServiceImpl;
import com.group.docorofile.services.specifications.UserSpecifications;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Autowired
    private UserRepository userRepository;

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
            @RequestParam(defaultValue = "1") int draw,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String plan,
            @RequestParam(defaultValue = "status") String status,
            @RequestParam(defaultValue = "modifiedOn,desc") String[] sort
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort[1]), sort[0]));
            Specification<UserEntity> spec = UserSpecifications.withFilters(search, role, plan, status);
            Page<UserEntity> usersPage = userRepository.findAll(spec, pageable);

            List<UserInfoDTO> userInfoList = usersPage.getContent().stream().map(user -> {
                UserInfoDTO dto = new UserInfoDTO();
                dto.setId(user.getUserId().toString());
                dto.setEmail(user.getEmail());
                dto.setName(user.getFullName());
                dto.setRole(user instanceof AdminEntity ? "Admin" :
                        user instanceof ModeratorEntity ? "Moderator" : "Member");
                if (user instanceof MemberEntity) {
                    dto.setCurrent_plan(((MemberEntity) user).getMembership().getLevel().name());
                } else {
                    dto.setCurrent_plan("");
                }
                dto.setStatus(user.isActive() ? "Active" : "Inactive");
                return dto;
            }).toList();

            return ResponseEntity.ok(new DataTableResponse<>(draw, usersPage.getTotalElements(), usersPage.getTotalElements(), userInfoList));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DataTableResponse<>(draw, 0, 0, Collections.emptyList()));
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
    public ResponseEntity<SuccessResponse> deactivateUser(@PathVariable String id) {
        UUID userId = UUID.fromString(id);
        UserEntity user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundError("User not found"));
        userService.deactivateUser(userId);
        SuccessResponse response = new SuccessResponse("User deactivated successfully", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse> getUserStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("totalMembers", userService.getTotalMembers());
        stats.put("inactiveMembers", userService.getInactiveMembers());
        stats.put("totalMembersWithPlan", userService.getTotalMembersWithPlan("PREMIUM"));
        SuccessResponse response = new SuccessResponse("User statistics retrieved successfully", HttpStatus.OK.value(), stats, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}