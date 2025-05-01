package com.group.docorofile.controllers.management.report;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManageReportController {
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/moderator/manage-reports")
    public String showManageReportPageOfModerator() {
        return "report/reports";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/manage-reports")
    public String showManageReportPageOfAdmin() {
        return "report/reports";
    }
}
