package com.group.docorofile.security;

import com.group.docorofile.exceptions.CustomAccessDeniedHandler;
import com.group.docorofile.exceptions.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                                                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
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

                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

}