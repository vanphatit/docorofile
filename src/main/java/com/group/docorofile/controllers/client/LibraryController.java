package com.group.docorofile.controllers.client;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/library")
public class LibraryController {

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @RequestMapping
    public String viewLibrary() {
        return "document/library";
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @RequestMapping("/view-more")
    public String viewMore() {
        return "document/library_viewmore";
    }
}
