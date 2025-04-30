package com.group.docorofile.services;


import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.dto.UserDetailDTO;
import com.group.docorofile.models.dto.UserInfoDTO;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.models.users.UpdateUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface iUserService {
    // Tạo user dựa trên loại được chỉ định trong request
    UserEntity createMember(CreateUserRequest request);

    UserEntity createManager(CreateUserRequest request);

    Optional<UserEntity> getUserById(UUID id);

    UserInfoDTO getUserInfoById(UUID id);

    UserDetailDTO getUserDetailById(UUID id);

    Page<UserEntity> getAllUsers(Pageable pageable);

    boolean checkMembership(UUID userId);

    boolean upgradeMembership(UUID userId, String plan);

    boolean existsByEmail(String email);

    boolean existsByUserIdAndIsActive(UUID userId, boolean isActive);

    UserEntity updateMyProfile(UUID id, UpdateProfileRequest request);

    UserEntity updateUserByID(UUID id, UpdateUserRequest request);

    boolean changePasswordById(UUID id, String newPassword);

    void deactivateUser(UUID id);

    boolean courseFollowedByMember(UUID memberId);

    void activateUser(UUID id);

    int getTotalUsers();

    int getTotalMembers();

    int getInactiveMembers();

    int getTotalMembersWithPlan(String plan);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findById(UUID id);
}
