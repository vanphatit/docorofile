package com.group.docorofile.repositories;

import com.group.docorofile.entities.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, UUID> {

}
