package com.group.docorofile.services;

import com.group.docorofile.models.dto.ChatRoomRequest;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface iChatService {
    void createChatRoom(ChatRoomRequest request);
    void addMemberToChatRoom(UUID chatRoomId, UUID userId);
    void removeMemberFromChatRoom(UUID chatRoomId, UUID userId);
    ResultPaginationDTO getChatRoomsByMember(UUID userId, Pageable pageable);
}
