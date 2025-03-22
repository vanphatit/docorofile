package com.group.docorofile.controllers.v1.api.auth;

import com.group.docorofile.models.users.LoginRequest;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.CustomUserDetails;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.services.EmailService;
import com.group.docorofile.services.impl.UserServiceImpl;
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

    @Autowired
    private EmailService emailVerificationService;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);

        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setHttpOnly(true); // Không cho phép truy cập từ JavaScript để giảm rủi ro XSS
        jwtCookie.setPath("/"); // Áp dụng cho toàn bộ ứng dụng

        response.addCookie(jwtCookie);

        SuccessResponse successResponse  = new SuccessResponse("Login successful", HttpStatus.OK.value(), token, LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Tạo cookie mới có tên "JWT" với giá trị rỗng và maxAge = 0 để xóa cookie khỏi trình duyệt
        Cookie jwtCookie = new Cookie("JWT", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);

        response.addCookie(jwtCookie);

        SuccessResponse successResponse = new SuccessResponse("Logout successful", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("email") String email, @RequestParam("code") String code) {
        String storedCode = emailVerificationService.getVerificationCode(email);
        if(storedCode == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Không tìm thấy mã xác thực cho email này. Có thể đã hết hạn hoặc chưa đăng ký?");
        }

        if(!storedCode.equals(code)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Mã xác thực không chính xác, vui lòng thử lại.");
        }

        var optionalUser = userServiceImpl.findByEmail(email);
        if(optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy user với email: " + email);
        }

        var user = optionalUser.get();
        if(user.isActive()) {
            return ResponseEntity.ok("Tài khoản này đã được kích hoạt trước đó.");
        }

        user.setActive(true);
        userRepository.save(user);

        emailVerificationService.removeVerificationCode(email);

        SuccessResponse successResponse = new SuccessResponse("Xác thực email thành công, tài khoản đã được kích hoạt!", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }
}