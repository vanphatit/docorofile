package com.group.docorofile.services;


import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface iUserService {
    // Tạo user dựa trên loại được chỉ định trong request
    UserEntity createMember(CreateUserRequest request);

    UserEntity createManager(CreateUserRequest request);

    Optional<UserEntity> getUserById(UUID id);

    List<UserEntity> getAllUsers();

    boolean existsByEmail(String email);

    boolean existsByUserIdAndIsActive(UUID userId, boolean isActive);

    // Dùng lại CreateUserRequest để update (có thể tạo riêng DTO UpdateUserRequest nếu cần)
    UserEntity updateUser(UUID id, CreateUserRequest request);

    void deleteUser(UUID id);

    boolean courseFollowedByMember(UUID memberId);

    Optional<UserEntity> findByEmail(String email);
}
