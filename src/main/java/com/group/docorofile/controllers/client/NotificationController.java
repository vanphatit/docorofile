package com.group.docorofile.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationController {
    @GetMapping("/notifications")
    public String showManageReportPage() {
        return "notification/all_notifications";
    }
}
