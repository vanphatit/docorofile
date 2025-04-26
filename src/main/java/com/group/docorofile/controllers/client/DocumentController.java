package com.group.docorofile.controllers.client;

import com.group.docorofile.security.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @GetMapping("/{id}")
    public String viewDocument(@PathVariable UUID id, Model model) {
        model.addAttribute("id", id);
        return "document/document_detail";
    }
}

