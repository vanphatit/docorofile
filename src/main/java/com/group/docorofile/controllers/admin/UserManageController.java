package com.group.docorofile.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class UserManageController {

    @GetMapping("/users")
    public String showManageUserPage() {
        return "fragments/users/user-list";
    }
}
