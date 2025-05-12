package com.group.docorofile.controllers.client;

import com.group.docorofile.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {
    @GetMapping("/report")
    public String showReportForm(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
//        model.addAttribute("currentUser", currentUser.getUser());
        return "fragments/report/reportForm";
    }
}
