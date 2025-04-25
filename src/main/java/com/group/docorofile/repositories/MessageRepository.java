package com.group.docorofile.repositories;

import com.group.docorofile.entities.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
    @Query("SELECT m FROM MessageEntity m WHERE m.chatRoom.chatRoomId = :chatRoomId ORDER BY m.sentAt DESC")
    Page<MessageEntity> findMessageByChatRoomId(@Param("chatRoomId") UUID chatRoomId, Pageable pageable);

}
