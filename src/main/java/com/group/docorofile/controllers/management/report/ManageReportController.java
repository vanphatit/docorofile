package com.group.docorofile.controllers.management.report;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManageReportController {

    @GetMapping("/reports")
    public String showManageReportPage() {
        return "report/reports";
    }
}
