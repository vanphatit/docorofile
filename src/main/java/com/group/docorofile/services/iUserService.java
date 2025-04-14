package com.group.docorofile.services;


import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.UpdateProfileRequest;
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

    Page<UserEntity> getAllUsers(Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByUserIdAndIsActive(UUID userId, boolean isActive);

    UserEntity updateMyProfile(UUID id, UpdateProfileRequest request);

    UserEntity updateUserByID(UUID id, CreateUserRequest request);

    void deactivateUser(UUID id);

    boolean courseFollowedByMember(UUID memberId);

    Optional<UserEntity> findByEmail(String email);
}
