package com.group.docorofile.services;

import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.request.MessageRequest;
import com.group.docorofile.response.MessageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

public interface iMessageService {
    MessageResponse sendMessageToRoom(UUID senderId, MessageRequest request);
    ResultPaginationDTO getRecentMessagesByChatRoomId(UUID userId, UUID roomId, Pageable pageable);
}
