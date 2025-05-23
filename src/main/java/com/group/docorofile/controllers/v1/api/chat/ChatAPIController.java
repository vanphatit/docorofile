package com.group.docorofile.controllers.v1.api.chat;

import com.group.docorofile.models.dto.ChatRoomRequest;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.security.CustomUserDetails;
import com.group.docorofile.services.iChatService;
import com.group.docorofile.services.iMessageService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/chats")
public class ChatAPIController {

    private final iChatService chatService;
    private final iMessageService messageService;

    public ChatAPIController(iChatService chatService, iMessageService messageService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PostMapping("/create")
    public ResponseEntity<SuccessResponse> createChatRoom(@RequestBody ChatRoomRequest request) {
        chatService.createChatRoom(request);
        SuccessResponse response = new SuccessResponse(
                "Chat room created successfully",
                HttpStatus.CREATED.value(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_MEMBER')")
    @PostMapping("/{chatRoomId}/add-member/{userId}")
    public ResponseEntity<SuccessResponse> addMemberToChatRoom(
            @PathVariable UUID chatRoomId,
            @PathVariable UUID userId) {
        chatService.addMemberToChatRoom(chatRoomId, userId);
        SuccessResponse response = new SuccessResponse(
                "Member added successfully",
                HttpStatus.OK.value(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{chatRoomId}/remove-member/{userId}")
    public ResponseEntity<SuccessResponse> removeMemberFromChatRoom(
            @PathVariable UUID chatRoomId,
            @PathVariable UUID userId) {
        chatService.removeMemberFromChatRoom(chatRoomId, userId);
        SuccessResponse response = new SuccessResponse(
                "Member removed successfully",
                HttpStatus.OK.value(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_MEMBER')")
    @GetMapping("/rooms")
    public ResponseEntity<ResultPaginationDTO> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {

        UUID userId = currentUser.getUser().getUserId();

        return ResponseEntity.ok().body(chatService.getChatRoomsByMember(userId, pageable));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_MEMBER')")
    @GetMapping("/messages")
    public ResponseEntity<ResultPaginationDTO> getMessages(
            @RequestParam UUID roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 6, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {

                UUID userId = currentUser.getUser().getUserId();

        ResultPaginationDTO result = messageService.getRecentMessagesByChatRoomId(userId, roomId, pageable);
        return ResponseEntity.ok(result);
    }

}
