package com.group.docorofile.controllers.v1.api.user;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.AdminEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.ModeratorEntity;
import com.group.docorofile.models.dto.DataTableResponse;
import com.group.docorofile.models.dto.UserDetailDTO;
import com.group.docorofile.models.dto.UserInfoDTO;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.models.users.UpdateUserRequest;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.response.CreatedResponse;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.CustomUserDetails;
import com.group.docorofile.services.impl.UserServiceImpl;
import com.group.docorofile.services.specifications.UserSpecifications;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/v1/api/users")
@RequiredArgsConstructor
public class UserInfoAPIController {

    private final UserServiceImpl userService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

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
        UserInfoDTO user = userService.getUserInfoById(id);
        if( user == null) {
            throw new NotFoundError("User not found");
        }
        SuccessResponse response = new SuccessResponse("User retrieved successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @GetMapping("/user-detail/{userId}")
    public ResponseEntity<SuccessResponse> getUserDetail(@PathVariable UUID userId,
                                                         Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if(userId == null) {
            userId = user.getUserId();
        } else if(!user.getUserId().equals(userId ) ) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        }

        UserDetailDTO userInfo = userService.getUserDetailById(userId);
        if( userInfo == null) {
            throw new NotFoundError("User not found");
        }
        SuccessResponse response = new SuccessResponse("User retrieved successfully", HttpStatus.OK.value(), userInfo, LocalDateTime.now());
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

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateUser(@PathVariable UUID id,
                                                      @ModelAttribute UpdateUserRequest request,
                                                      Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if(id == null) {
            id = user.getUserId();
        } else if(!user.getUserId().equals(id) ) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        }
        userService.updateUserByID(id, request);
        return ResponseEntity.ok(new SuccessResponse("User updated successfully", 200, null, LocalDateTime.now()));
    }


    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PutMapping("/updateMyProfile")
    public ResponseEntity<SuccessResponse> updateMyProfile(@PathVariable UUID id, @RequestBody UpdateProfileRequest request) {
        UserEntity user = userService.updateMyProfile(id, request);
        SuccessResponse response = new SuccessResponse("User updated successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // Huỷ kích hoạt user
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deactivateUser(@PathVariable String id,
                                                          Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        UUID userId = null;

        if(id == null) {
            userId = user.getUserId();
        } else if(!user.getUserId().equals(UUID.fromString(id) ) ) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        } else {
            userId = UUID.fromString(id);
        }

        userService.deactivateUser(userId);
        SuccessResponse response = new SuccessResponse("User deactivated successfully", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // Kích hoạt user
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PostMapping("/activate/{id}")
    public ResponseEntity<SuccessResponse> activateUser(@PathVariable String id,
                                                        Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        UUID userId = null;

        if(id == null) {
            userId = user.getUserId();
        } else if(!user.getUserId().equals(UUID.fromString(id) ) ) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        } else {
            userId = UUID.fromString(id);
        }

        userService.activateUser(userId);
        SuccessResponse response = new SuccessResponse("User deactivated successfully", HttpStatus.OK.value(), null, LocalDateTime.now());
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

    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_ADMIN')")
    @PostMapping("/checkMembership/{userId}")
    public ResponseEntity<SuccessResponse> checkAndCreateMembership(@PathVariable UUID userId,
                                                                    Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if(userId == null) {
            userId = user.getUserId();
        } else if(!user.getUserId().equals(userId ) ) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        }
        boolean result = userService.checkMembership(userId);
        SuccessResponse response = new SuccessResponse("Checked membership successfully", HttpStatus.OK.value(), result, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PostMapping("/upgrade-plan/{userId}")
    public ResponseEntity<SuccessResponse> upgradeMembership(
            @PathVariable UUID userId,
            @RequestParam("plan") String plan, Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if(userId == null) {
            userId = user.getUserId();
        } else if(!user.getUserId().equals(UUID.fromString(userId.toString()) ) ) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        }

        boolean result = userService.upgradeMembership(userId, plan.toUpperCase());
        SuccessResponse response = new SuccessResponse("Membership upgraded successfully", HttpStatus.OK.value(), result, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam("userId") String id,
                                            @RequestParam("newPassword") String newPassword,
                                            Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        UUID userId = null;

        if(id == null) {
            userId = user.getUserId();
        } else if(!user.getUserId().equals(UUID.fromString(id) ) ) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        } else {
            userId = UUID.fromString(id);
        }

        SuccessResponse successResponse = new SuccessResponse(
                "Đổi mật khẩu thành công!", HttpStatus.OK.value(),
                userService.changePasswordById(userId, newPassword), LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }

}