package com.group.docorofile.services.factories;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.MembershipEntity;
import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.repositories.UniversityRepository;
import com.group.docorofile.services.impl.UniversityServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemberFactory implements iUserFactory {

    private final UniversityServiceImpl universityService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(String userType) {
        return "MEMBER".equalsIgnoreCase(userType);
    }

    @Override
    public UserEntity createUser(CreateUserRequest request) {
        // Tạo membership mặc định
        MembershipEntity membership = MembershipEntity.builder()
                .level(EMembershipLevel.FREE)
                .startDate(LocalDateTime.now())
                .build();

        // Lấy trường đại học nếu có
        UniversityEntity university = null;
        String univName = request.getUniversityName(); // giả sử CreateUserRequest có field này
        if (univName != null) {
            university = universityService.findByUnivName(univName);
            if( university == null) {
                throw new RuntimeException("University not found with name: " + univName);
            }
        }

        return MemberEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(false)
                .downloadLimit(Optional.ofNullable(request.getDownloadLimit()).orElse(0))
                .isChat(request.getIsChat() == null ? Boolean.TRUE : request.getIsChat())
                .isComment(request.getIsComment() == null ? Boolean.FALSE : request.getIsComment())
                .membership(membership)
                .studyAt(university)
                .build();
    }
}