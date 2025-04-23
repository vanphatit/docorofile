package com.group.docorofile.services.impl;

import com.group.docorofile.entities.ChatRoomEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.MessageEntity;
import com.group.docorofile.repositories.ChatRoomRepository;
import com.group.docorofile.repositories.MemberRepository;
import com.group.docorofile.repositories.MessageRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.request.MessageRequest;
import com.group.docorofile.response.MessageResponse;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.services.iMessageService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessageServiceImpl implements iMessageService {
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public MessageServiceImpl(MessageRepository messageRepository,
                              ChatRoomRepository chatRoomRepository, MemberRepository memberRepository,
                              SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.memberRepository = memberRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @Override
    public MessageResponse sendMessageToRoom(UUID senderId, MessageRequest request) {
        ChatRoomEntity chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new NotFoundError("Room not found"));

        MemberEntity member = memberRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundError("Sender is not a member"));

        MessageEntity message = MessageEntity.builder()
                .chatRoom(chatRoom)
                .sender(member)
                .content(request.getContent())
                .build();

        messageRepository.save(message);

        MessageResponse response = MessageResponse.builder()
                .id(message.getMessageId())
                .content(message.getContent())
                .sender(member.getEmail())
                .senderId(member.getUserId())
                .roomId(chatRoom.getChatRoomId())
                .roomName(chatRoom.getTitle())
                .timestamp(message.getSentAt())
                .type(MessageResponse.MessageType.CHAT)
                .build();

        // Gửi WebSocket cho các client đang subscribe
        messagingTemplate.convertAndSend("/topic/room." + chatRoom.getChatRoomId(), response);

        return response;
    }
}
