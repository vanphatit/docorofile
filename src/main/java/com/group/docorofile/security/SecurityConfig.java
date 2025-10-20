package com.group.docorofile.security;

import com.group.docorofile.exceptions.CustomAccessDeniedHandler;
import com.group.docorofile.exceptions.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public CustomCsrfTokenRepository csrfTokenRepository() {
        CustomCsrfTokenRepository repository = new CustomCsrfTokenRepository();
        repository.setCookieHttpOnly(true);
        repository.setSecure(true); // Always use secure cookies in production
        repository.setSameSite("Strict"); // More secure
        repository.setCookieMaxAge(3600); // 1 hour
        return repository;
    }

    @Bean
    public CspNonceFilter cspNonceFilter() { return new CspNonceFilter(); }

    @Bean
    public FilterRegistrationBean<CspNonceFilter> cspNonceFilterRegistration() {
        FilterRegistrationBean<CspNonceFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new CspNonceFilter());
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE); // chạy trước mọi filter khác
        reg.addUrlPatterns("/*");
        return reg;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .ignoringRequestMatchers(
                                "/v1/api/**", // Exclude all API endpoints from CSRF
                                "/oauth2/authorization/**",
                                "/member/payment/**" // Exclude all payment endpoints
                                // from CSRF
                        ))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true))
                        .referrerPolicy(referrerPolicy -> referrerPolicy.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
//                        .addHeaderWriter(cspHeaderWriter())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/api/auth/**",
                                "/auth/**",
                                "/oauth2/authorization/google",
                                "/error",
                                "/documents/**",
                                "/uploads/documents/**",
                                "/v1/api/documents/view/**",
                                "/v1/api/documents/search/**",
                                "/v1/api/documents/filter",
                                "/v1/api/documents/related/**",
                                "/v1/api/documents/metadata",
                                "/v1/api/documents/search/suggestions",
                                "/v1/api/reactions/count/",
                                "/v1/api/reactions/status",
                                "/v1/api/comments/**",
                                "/v1/api/universities/names")
                        .permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers(
                                "/member/payment/vn-pay-callback")
                        .permitAll()
                        .requestMatchers(
                                "/assets/**",
                                "/templates/**",
                                "/static/**",
                                "/favicon.ico",
                                "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/v1/api/users/newMember").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .oauth2Login(login -> login
                        .loginPage("/au/login")
                        .successHandler((request, response, authentication) -> request
                                .getRequestDispatcher("/auth/login/oauth2Google-submit")
                                .forward(request, response))
                        .permitAll());

        // Đặt trước HeaderWriterFilter để header CSP được ghi đúng lúc
        http.addFilterBefore(cspNonceFilter(), SecurityContextHolderFilter.class);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public StaticHeadersWriter cspHeaderWriter() {
        return new StaticHeadersWriter("Content-Security-Policy",
                "default-src 'self' https:; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://unpkg.com https://sandbox.vnpayment.vn https://code.jquery.com; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; " +
                        "font-src 'self' data: https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                        "img-src 'self' data: https://cdn.jsdelivr.net https://unpkg.com https://sandbox.vnpayment.vn; " +
                        "frame-src 'self' https://sandbox.vnpayment.vn; " +
                        "connect-src 'self' https://localhost:9091 https://sandbox.vnpayment.vn; " +
                        "form-action 'self' https://sandbox.vnpayment.vn;");
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Chỉ cho origin WHITELIST – không dùng *
        cfg.setAllowedOrigins(List.of(
                "http://localhost:9091",
                "https://localhost:9091",
                "https://cdn.jsdelivr.net",
                "https://unpkg.com"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // CHỈ bật CORS cho các API được gọi từ browser
        source.registerCorsConfiguration("/api/public/**", cfg);

        // Không đăng ký cho /admin/**, /internal/** → mặc định KHÔNG CORS
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}