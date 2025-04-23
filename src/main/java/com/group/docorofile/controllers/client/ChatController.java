package com.group.docorofile.controllers.client;

import com.group.docorofile.security.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @GetMapping("/chats")
    public String showChatRoom(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("currentUser", currentUser.getUser());
        return "chat/chat_room";
    }
}
