package com.group.docorofile.services;

import com.group.docorofile.request.MessageRequest;
import com.group.docorofile.response.MessageResponse;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

public interface iMessageService {
    MessageResponse sendMessageToRoom(UUID senderId, MessageRequest request);
}
