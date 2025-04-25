package com.group.docorofile.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model m) {
        m.addAttribute("page","home");
        m.addAttribute("pageTitle","Home – DoCoroFile");
        return "fragments/index";
    }
}
