package com.group.docorofile.controllers.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.docorofile.security.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    @GetMapping("/{id}")
    public String viewDocument(@PathVariable UUID id, Model model) {
        model.addAttribute("id", id);
        return "document/document_detail";
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @GetMapping("/uploads")
    public String viewUploads() {
        try {
            return "document/document_uploads";
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @GetMapping("/metadata-form")
    public String metadataForm(@RequestParam(name = "ids") List<String> ids, Model model) {
        try {
            model.addAttribute("documentIds", new ObjectMapper().writeValueAsString(ids)); // Chuyển đổi danh sách UUID thành chuỗi JSON
            return "document/metadata-form";
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi thật
            throw new RuntimeException("Lỗi xử lý UUID: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @GetMapping("/success")
    public String success() {
        return "document/success_form";
    }

    @GetMapping("/search")
    public String searchDocument(@RequestParam(name = "keyword") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        return "document/search";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/manage")
    public String adminDocument(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userDetails);
        return "document/manage_document";
    }
}

