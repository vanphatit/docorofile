package com.group.docorofile.services.factories;

import com.group.docorofile.entities.AdminEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminFactory implements iUserFactory {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(String userType) {
        return "ADMIN".equalsIgnoreCase(userType);
    }

    @Override
    public UserEntity createUser(CreateUserRequest request) {
        return AdminEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();
    }
}
