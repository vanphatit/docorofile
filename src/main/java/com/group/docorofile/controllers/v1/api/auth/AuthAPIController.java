package com.group.docorofile.controllers.v1.api.auth;

import com.group.docorofile.models.users.LoginRequest;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.CustomUserDetails;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.response.SuccessResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/api/auth")
public class AuthAPIController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);

        // Tạo cookie chứa JWT
        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setHttpOnly(true); // Không cho phép truy cập từ JavaScript để giảm rủi ro XSS
        jwtCookie.setPath("/"); // Áp dụng cho toàn bộ ứng dụng

        // Thêm cookie vào response
        response.addCookie(jwtCookie);

        SuccessResponse successResponse  = new SuccessResponse("Login successful", HttpStatus.OK.value(), token, LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Tạo cookie mới có tên "JWT" với giá trị rỗng và maxAge = 0 để xóa cookie khỏi trình duyệt
        Cookie jwtCookie = new Cookie("JWT", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");      // Áp dụng cho toàn bộ ứng dụng
        jwtCookie.setMaxAge(0);      // Set maxAge = 0 để xóa cookie

        response.addCookie(jwtCookie);

        SuccessResponse successResponse = new SuccessResponse("Logout successful", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }
}