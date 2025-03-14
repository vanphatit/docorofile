package com.group.docorofile.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.docorofile.response.ForbiddenError;
import com.group.docorofile.response.UnauthorizedError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Đặt header kiểu JSON
        response.setContentType("application/json");
        // Đặt status code 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        UnauthorizedError errorResponse = new UnauthorizedError("Forbidden access");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}