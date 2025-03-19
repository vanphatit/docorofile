package com.group.docorofile.repositories;

import com.group.docorofile.entities.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    @Query("SELECT n FROM NotificationEntity n WHERE n.receiver.userId = :receiverId ORDER BY n.createdOn DESC")
    Page<NotificationEntity> findAllByReceiverId(@Param("receiverId") UUID receiverId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.receiver.userId = FUNCTION('UUID_TO_BIN', :receiverId) ORDER BY n.createdOn DESC")
    Page<NotificationEntity> findAllByReceiverId(@Param("receiverId") String receiverId, Pageable pageable);
}
