package com.group.docorofile.repositories;

import com.group.docorofile.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByEmail(String email);
    // check email exists
    boolean existsByEmail(String email);
    // check user active
    boolean existsByUserIdAndIsActive(UUID userId, boolean isActive);
}