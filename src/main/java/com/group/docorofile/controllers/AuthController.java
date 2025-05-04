package com.group.docorofile.controllers;

import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.LoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class AuthController {

    @GetMapping("/auth/register")
    public String register(Model m) {
        m.addAttribute("page","register");
        m.addAttribute("pageTitle","Register – DoCoroFile");
        m.addAttribute("createUserRequest", new CreateUserRequest());
        return "fragments/auth/register";
    }

    @GetMapping("/auth/login")
    public String login(Model m) {
        m.addAttribute("page","login");
        m.addAttribute("pageTitle","Login – DoCoroFile");
        m.addAttribute("loginRequest", new LoginRequest());
        return "fragments/auth/login";
    }

    @GetMapping("/auth/logout")
    public String logout(HttpServletResponse response, Model m) {
        try {
            // Call REST API endpoint (API nội bộ)
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:9091/v1/api/auth/logout";
            ResponseEntity<Map> res = restTemplate.postForEntity(url, null, Map.class);

            Cookie cookie = new Cookie("JWT", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            m.addAttribute("success", "Logout successful.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            return "fragments/auth/login";
        }
    }

    @PostMapping("/auth/login")
    public String login(@ModelAttribute LoginRequest loginRequest, Model m, HttpServletResponse response) {
        try {
            // Call REST API endpoint (API nội bộ)
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:9091/v1/api/auth/login";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoginRequest> entity = new HttpEntity<>(loginRequest, headers);

            ResponseEntity<Map> res = restTemplate.postForEntity(url, entity, Map.class);

            if (res.getStatusCode().is2xxSuccessful()) {
                String token = res.getBody().get("data").toString();
                Cookie cookie = new Cookie("JWT", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(3600);
                response.addCookie(cookie);

                m.addAttribute("success", "Login successful.");
                return "redirect:/";
            } else {
                m.addAttribute("error", "Login failed.");
                return "fragments/auth/login";
            }
        } catch (Exception e) {
            m.addAttribute("error", e.getMessage());
            return "fragments/auth/login";
        }
    }

    @GetMapping("/auth/verify-email")
    public String verifyEmail(@RequestParam("code") String code, @RequestParam("email") String email, Model model) {
        // Call REST API endpoint (API nội bộ)
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:9091/v1/api/auth/verify-email?code=" + code + "&email=" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("success", "Email verified successfully.");
            return "redirect:/auth/login";
        } else {
            model.addAttribute("error", "Email verification failed.");
            return "fragments/auth/register";
        }
    }

    @PostMapping("/auth/register")
    public String processRegisterForm(@ModelAttribute CreateUserRequest request,
                                      @RequestParam String rePassword,
                                      Model model) {
        if (!request.getPassword().equals(rePassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("createUserRequest", request);
            return "fragments/auth/register";
        }

        request.setUserType("MEMBER");

        try {
            // Call REST API endpoint (API nội bộ)
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:9091/v1/api/users/newMember";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("success", "Registration successful. Please check your email.");
                model.addAttribute("email", request.getEmail());
                return "fragments/auth/verify-email";
            } else {
                model.addAttribute("error", "Registration failed.");
                return "fragments/auth/register";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "fragments/auth/register";
        }
    }

    @GetMapping("/auth/login/oauth2Google-submit")
    public String loginGoogle(OAuth2AuthenticationToken oauth2Token, Model m, HttpServletResponse response) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:9091/v1/api/auth/login/oauth2";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Truyền email và name thủ công thay vì truyền OAuth2AuthenticationToken
            Map<String, String> oauthUser = new HashMap<>();
            oauthUser.put("email", oauth2Token.getPrincipal().getAttribute("email"));
            oauthUser.put("name", oauth2Token.getPrincipal().getAttribute("name"));

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(oauthUser, headers);
            ResponseEntity<Map> res = restTemplate.postForEntity(url, entity, Map.class);

            if (res.getStatusCode().is2xxSuccessful()) {
                String token = res.getBody().get("data").toString();
                Cookie cookie = new Cookie("JWT", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(3600);
                response.addCookie(cookie);

                return "redirect:/";
            } else {
                m.addAttribute("error", "Login failed: " + res.getBody().get("message"));
                m.addAttribute("loginRequest", new LoginRequest()); // tránh lỗi Thymeleaf
                return "fragments/auth/login";
            }
        } catch (Exception e) {
            m.addAttribute("error", "Exception: " + e.getMessage());
            m.addAttribute("loginRequest", new LoginRequest()); // tránh lỗi Thymeleaf
            return "fragments/auth/login";
        }
    }
}
