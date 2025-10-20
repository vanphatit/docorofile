package com.group.docorofile.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CspNonceModelAdvice {

    @ModelAttribute("cspNonce")
    public String exposeNonce(HttpServletRequest req) {
        Object n = req != null ? req.getAttribute(CspNonceFilter.ATTR) : null;
        return n != null ? n.toString() : "";
    }
}