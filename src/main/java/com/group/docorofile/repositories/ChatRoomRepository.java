package com.group.docorofile.repositories;

import com.group.docorofile.entities.ChatRoomEntity;
import com.group.docorofile.entities.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, UUID> {
    Page<ChatRoomEntity> findByMembers_UserId(UUID userId, Pageable pageable);
    Optional<ChatRoomEntity> findByCourse_CourseId(UUID courseId);
}
