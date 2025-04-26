package com.group.docorofile.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(@CookieValue(name = "token", required = false) String token, Model m, HttpServletRequest request) {
        m.addAttribute("page","home");
        m.addAttribute("pageTitle","Home – DoCoroFile");
        if(token != null) {

        }

        return "fragments/index";
    }
}
