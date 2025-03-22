package com.group.docorofile.services.impl;

import com.group.docorofile.entities.AdminEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.ModeratorEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.repositories.FollowCourseRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.response.BadRequestError;
import com.group.docorofile.response.ConflictError;
import com.group.docorofile.response.InternalServerError;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.services.EmailService;
import com.group.docorofile.services.iUserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements iUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowCourseRepository followCourseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // Tạo user dựa trên loại được chỉ định trong request
    @Override
    public UserEntity createMember(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictError("User already exists with email: " + request.getEmail());
        }

        UserEntity user;
        user = MemberEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(false)
                .downloadLimit(request.getDownloadLimit() != null ? request.getDownloadLimit() : 0)
                .isChat(request.getIsChat() != null ? request.getIsChat() : false)
                .isComment(request.getIsComment() != null ? request.getIsComment() : false)
                .build();

        try {
            // random 6 chữ số
            int code = (int)((Math.random() * 900000) + 100000);
            String verificationCode = String.valueOf(code);

            // Gửi email
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        } catch (MessagingException e) {
            throw new InternalServerError("Could not send verification email");
        }

        return userRepository.save(user);
    }

    @Override
    public UserEntity createManager(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictError("User already exists with email: " + request.getEmail());
        }

        UserEntity user;
        String userType = request.getUserType();
        if ("ADMIN".equalsIgnoreCase(userType)) {
            user = AdminEntity.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .password(request.getPassword()) // sẽ mã hoá sau
                    .isActive(true)
                    .build();
        } else if ("MODERATOR".equalsIgnoreCase(userType)) {
            user = ModeratorEntity.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .isActive(true)
                    .isReportManage(request.getIsReportManage() != null ? request.getIsReportManage() : false)
                    .build();
        } else {
            throw new BadRequestError("Invalid user type: " + userType);
        }
        // Mã hoá password trước khi lưu
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<UserEntity> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUserIdAndIsActive(UUID userId, boolean isActive) {
        return userRepository.existsByUserIdAndIsActive(userId, isActive);
    }

    // Dùng lại CreateUserRequest để update
    @Override
    public UserEntity updateUser(UUID id, CreateUserRequest request) {
        Optional<UserEntity> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new NotFoundError("User not found with id: " + id);
        }
        UserEntity user = optUser.get();
        // Cập nhật các field chung
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        // Mã hoá password khi update
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Cập nhật các field riêng tùy theo loại
        if (user instanceof ModeratorEntity) {
            ModeratorEntity mod = (ModeratorEntity) user;
            mod.setReportManage(request.getIsReportManage() != null ? request.getIsReportManage() : mod.isReportManage());
        } else if (user instanceof MemberEntity) {
            MemberEntity member = (MemberEntity) user;
            member.setDownloadLimit(request.getDownloadLimit() != null ? request.getDownloadLimit() : member.getDownloadLimit());
            member.setChat(request.getIsChat() != null ? request.getIsChat() : member.isChat());
            member.setComment(request.getIsComment() != null ? request.getIsComment() : member.isComment());
            member.setMyProfile(request.getMyProfile() != null ? request.getMyProfile() : member.getMyProfile());
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        // Soft delete user by setting isActive to false
        Optional<UserEntity> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new NotFoundError("User not found with id: " + id);
        }
        UserEntity user = optUser.get();
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean courseFollowedByMember(UUID memberId) {
        return followCourseRepository.existsByFollower(memberId);
    }
}