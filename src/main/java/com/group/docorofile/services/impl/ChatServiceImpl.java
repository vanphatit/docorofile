package com.group.docorofile.services.impl;

import com.group.docorofile.entities.ChatRoomEntity;
import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.dto.ChatRoomRequest;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.repositories.ChatRoomRepository;
import com.group.docorofile.repositories.CourseRepository;
import com.group.docorofile.repositories.MemberRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.response.ChatRoomResponse;
import com.group.docorofile.services.iChatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatServiceImpl implements iChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;

    public ChatServiceImpl(
            ChatRoomRepository chatRoomRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            MemberRepository memberRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.courseRepository = courseRepository;
        this.memberRepository = memberRepository;
    }

    public void createChatRoom(ChatRoomRequest request) {
        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .title(request.getTitle())
                .course(course)
                .createdOn(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);
    }

    public void addMemberToChatRoom(UUID chatRoomId, UUID userId) {
        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatRoom.getMembers().add(member);
        chatRoomRepository.save(chatRoom);
    }

    public void removeMemberFromChatRoom(UUID chatRoomId, UUID userId) {
        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // Tìm đúng member trong danh sách thành viên theo ID
        MemberEntity memberToRemove = chatRoom.getMembers().stream()
                .filter(member -> member.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User is not a member of this chat room"));

        chatRoom.getMembers().remove(memberToRemove);
        chatRoomRepository.save(chatRoom);
    }

    public ResultPaginationDTO getChatRoomsByMember(UUID userId, Pageable pageable) {
        Page<ChatRoomEntity> page = chatRoomRepository.findByMembers_UserId(userId, pageable);

        List<ChatRoomResponse> dtoList = page.getContent()
                .stream()
                .map(chatRoom -> ChatRoomResponse.builder()
                        .id(chatRoom.getChatRoomId())
                        .title(chatRoom.getTitle())
                        .createdOn(chatRoom.getCreatedOn())
                        .totalMembers(chatRoom.getMembers() != null ? chatRoom.getMembers().size() : 0)
                        .build())
                .toList();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(dtoList);

        return result;
    }

}

