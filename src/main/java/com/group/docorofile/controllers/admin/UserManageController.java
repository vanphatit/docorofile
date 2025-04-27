package com.group.docorofile.controllers.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class UserManageController {

    @GetMapping("/users")
    public String showManageUserPage(Model m) {
        m.addAttribute("pageTitle","Users | Admin – DoCoroFile");
        return "fragments/users/user-list";
    }
}
