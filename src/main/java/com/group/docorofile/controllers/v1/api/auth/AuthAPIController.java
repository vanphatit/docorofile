package com.group.docorofile.controllers.v1.api.auth;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.dto.UserInfoDTO;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.LoginRequest;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.response.BadRequestError;
import com.group.docorofile.response.InternalServerError;
import com.group.docorofile.response.UnauthorizedError;
import com.group.docorofile.security.CustomUserDetails;
import com.group.docorofile.security.JwtTokenUtil;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.services.EmailService;
import com.group.docorofile.services.impl.UserServiceImpl;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @PostMapping("/login/oauth2")
    public ResponseEntity<?> loginOAuth2(@RequestBody Map<String, String> oauthUser,
                                         HttpServletResponse response) {
        // Xử lý đăng nhập với OAuth2
        String email = oauthUser.get("email");
        String name = oauthUser.get("name");

        // Kiểm tra xem người dùng đã tồn tại trong hệ thống chưa
        UserEntity user = userServiceImpl.findByEmail(email).orElse(null);
        if (user == null) {
            user = userServiceImpl.createOAuthMember(email, name);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email, @RequestParam("newPassword") String newPassword) {
        var optUser = userServiceImpl.findByEmail(email);
        if(optUser.isEmpty()) {
            BadRequestError error = new BadRequestError("Không tìm thấy user với email: " + email);
            return ResponseEntity.badRequest().body(error);
        }

        try{
            int code = (int)((Math.random() * 900000) + 100000);
            String verifyCode = String.valueOf(code);

            emailVerificationService.sendResetPasswordEmail(email, verifyCode, newPassword);
        } catch (MessagingException e) {
            throw new InternalServerError("Không thể gửi email xác thực.");
        }

        SuccessResponse successResponse = new SuccessResponse("Đã gửi mã xác thực đặt lại mật khẩu về email của bạn. Vui lòng kiểm tra hộp thư!", HttpStatus.OK.value(), null, LocalDateTime.now());

        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("code") String code, @RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword) {
        // 1. Lấy code đã lưu
        String storedCode = emailVerificationService.getVerificationCode(email);
        if(storedCode == null) {
            BadRequestError error = new BadRequestError("Không tìm thấy mã xác thực cho email này. Có thể đã hết hạn hoặc chưa đăng ký?");
            return ResponseEntity.badRequest().body(error);
        }

        if(!storedCode.equals(code)) {
            BadRequestError error = new BadRequestError("Mã xác thực không chính xác, vui lòng thử lại.");
            return ResponseEntity.badRequest().body(error);
        }

        var optUser = userServiceImpl.findByEmail(email);
        if(optUser.isEmpty()) {
            BadRequestError error = new BadRequestError("Không tìm thấy user với email: " + email);
            return ResponseEntity.badRequest().body(error);
        }
        var user = optUser.get();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailVerificationService.removeVerificationCode(email);

        SuccessResponse successResponse = new SuccessResponse("Đặt lại mật khẩu thành công!", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_ADMIN', 'ROLE_MODERATOR')")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,
            Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        }
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            UnauthorizedError error = new UnauthorizedError("Mật khẩu cũ không chính xác!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        SuccessResponse successResponse = new SuccessResponse("Đổi mật khẩu thành công!", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_ADMIN', 'ROLE_MODERATOR')")
    @PostMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if(user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng!");
        }

        // Chỉ trả về thông tin cần thiết
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(user.getUserId().toString());
        userInfoDTO.setEmail(user.getEmail());
        userInfoDTO.setName(user.getFullName());
        userInfoDTO.setRole(currentUserDetails.getAuthorities().iterator().next().getAuthority());

        if(user instanceof MemberEntity){
            MemberEntity member = (MemberEntity) user;
            if(member.getMembership() == null) {
                throw new BadRequestError("Không tìm thấy thông tin thành viên!");
            }
            userInfoDTO.setCurrent_plan(member.getMembership().getLevel().toString());
        }
        SuccessResponse successResponse = new SuccessResponse("Lấy thông tin người dùng thành công!", HttpStatus.OK.value(), userInfoDTO, LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }
}