package com.group.docorofile.services.factories;

import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.MembershipEntity;
import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.repositories.UniversityRepository;
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

    private final UniversityRepository universityRepository;
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
        UUID univId = request.getUniversityId(); // giả sử CreateUserRequest có field này
        if (univId != null) {
            Optional<UniversityEntity> opt = universityRepository.findById(univId);
            if (opt.isPresent()) {
                university = opt.get();
            } else {
                throw new IllegalArgumentException("University not found with id: " + univId);
            }
        }

        return MemberEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(false)
                .downloadLimit(Optional.ofNullable(request.getDownloadLimit()).orElse(0))
                .isChat(Boolean.TRUE.equals(request.getIsChat()))
                .isComment(Boolean.TRUE.equals(request.getIsComment()))
                .membership(membership)
                .studyAt(university)
                .build();
    }
}