package com.group.docorofile.controllers.v1.api.chat;

import com.group.docorofile.request.MessageRequest;
import com.group.docorofile.response.MessageResponse;
import com.group.docorofile.security.CustomUserDetails;
import com.group.docorofile.services.impl.MessageServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/messages")
public class MessageAPIController {

    private final MessageServiceImpl messageService;

    public MessageAPIController(MessageServiceImpl messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody MessageRequest messageRequest) {

        UUID userId = currentUser.getUser().getUserId();

        MessageResponse response = messageService.sendMessageToRoom(userId, messageRequest);

        return ResponseEntity.ok(response);
    }

}
