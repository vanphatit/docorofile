package com.group.docorofile.services.impl;

import com.group.docorofile.entities.*;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.models.dto.UserInfoDTO;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.repositories.FollowCourseRepository;
import com.group.docorofile.repositories.MembershipRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.response.BadRequestError;
import com.group.docorofile.response.ConflictError;
import com.group.docorofile.response.InternalServerError;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.services.EmailService;
import com.group.docorofile.services.factories.iUserFactory;
import com.group.docorofile.services.iUserService;
import com.group.docorofile.services.strategies.iUserUpdateStrategy;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements iUserService {

    private final UserRepository userRepository;

    private final MembershipRepository membershipRepository;

    private final FollowCourseRepository followCourseRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final List<iUserFactory> userFactories;
    private final List<iUserUpdateStrategy> updateStrategies;

    private iUserFactory getFactory(String userType) {
        return userFactories.stream()
                .filter(factory -> factory.supports(userType))
                .findFirst()
                .orElseThrow(() -> new BadRequestError("Invalid user type: " + userType));
    }

    private iUserUpdateStrategy getUpdateStrategy(UserEntity user) {
        return updateStrategies.stream()
                .filter(strategy -> strategy.supports(user))
                .findFirst()
                .orElseThrow(() -> new BadRequestError("Unsupported user type"));
    }

    @Override
    public UserEntity createMember(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictError("User already exists with email: " + request.getEmail());
        }

        iUserFactory factory = getFactory("MEMBER");
        MemberEntity user = (MemberEntity) factory.createUser(request);

        try {//≥≤>
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

        iUserFactory factory = getFactory(request.getUserType());
        UserEntity user = factory.createUser(request);
        return userRepository.save(user);
    }

    @Override
    public Optional<UserEntity> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Page<UserEntity> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
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
    public UserEntity updateMyProfile(UUID id, UpdateProfileRequest request) {
        Optional<UserEntity> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new NotFoundError("User not found with id: " + id);
        }
        iUserUpdateStrategy updateStrategy = getUpdateStrategy(optUser.get());
        UserEntity user = optUser.get();
        updateStrategy.updateProfile(user, request);
        return userRepository.save(user);
    }

    @Override
    public UserEntity updateUserByID(UUID id, CreateUserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        getUpdateStrategy(user).update(user, request);
        return userRepository.save(user);
    }

    @Override
    public void deactivateUser(UUID id) {
        // Soft delete user by setting isActive to false
        Optional<UserEntity> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new NotFoundError("User not found with id: " + id);
        }
        UserEntity user = optUser.get();
        user.setActive(false);
        userRepository.save(user);
    }

    public int getTotalUsers() {
        return (int) userRepository.count();
    }

    public int getTotalMembers() {
        return userRepository.findAll().stream()
                .filter(user -> user instanceof MemberEntity)
                .mapToInt(user -> 1)
                .sum();
    }

    public int getInactiveMembers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isActive())
                .mapToInt(user -> 1)
                .sum();
    }

    public int getTotalMembersWithPlan(String plan) {
        return userRepository.findAll().stream()
                .filter(user -> user instanceof MemberEntity && ((MemberEntity) user).getMembership().getLevel().name().equals(plan) )
                .mapToInt(user -> 1)
                .sum();
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