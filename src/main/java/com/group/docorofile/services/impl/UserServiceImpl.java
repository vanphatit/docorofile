package com.group.docorofile.services.impl;

import com.group.docorofile.entities.*;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.models.dto.UserDetailDTO;
import com.group.docorofile.models.dto.UserInfoDTO;
import com.group.docorofile.models.university.UniversityNameDTO;
import com.group.docorofile.models.users.CreateUserRequest;
import com.group.docorofile.models.users.UpdateProfileRequest;
import com.group.docorofile.models.users.UpdateUserRequest;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements iUserService {

    private final UserRepository userRepository;

    private final MembershipRepository membershipRepository;

    private final FollowCourseRepository followCourseRepository;

    private final UniversityServiceImpl universityService;

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

    public UserEntity createOAuthMember(String email, String fullName) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictError("User already exists with email: " + email);
        }

        // Tạo request giả lập
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setFullName(fullName);
        request.setPassword("12312345"); // dummy password
        request.setDownloadLimit(0);
        request.setIsChat(true);
        request.setIsComment(true);

        // Lấy trường đại học nếu có
        UniversityNameDTO university = universityService.findAllUniversityNames().get(0);
        if (university != null) {
            request.setUniversityName(university.getUnivName());
        }
        request.setUserType("MEMBER");

        // Lấy factory tương ứng
        iUserFactory factory = getFactory("MEMBER");
        MemberEntity member = (MemberEntity) factory.createUser(request);

        // Ghi đè isActive = true (vì Google đã xác thực)
        member.setActive(true);

        return userRepository.save(member);
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
    public UserInfoDTO getUserInfoById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        UserInfoDTO dto = new UserInfoDTO();
        dto = dto.mapToUserInfo(user);
        return dto;
    }

    @Override
    public UserDetailDTO getUserDetailById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));
        return UserDetailDTO.mapTo(user);
    }

    @Override
    public Page<UserEntity> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public boolean checkMembership(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + userId));

        if (!(userEntity instanceof MemberEntity member)) {
            return false;
        }

        if (member.getMembership() == null) {
            MembershipEntity membership = new MembershipEntity();
            membership.setLevel(EMembershipLevel.FREE);
            membership.setStartDate(LocalDateTime.now());
            member.setMembership(membership);
            userRepository.save(member);
        } else {
            if( member.getMembership().getEndDate() != null) {
                if (member.getMembership().getEndDate().isBefore(LocalDateTime.now())) {
                    member.getMembership().setLevel(EMembershipLevel.FREE);
                    userRepository.save(member);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean upgradeMembership(UUID userId, String plan) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + userId));

        if (!(userEntity instanceof MemberEntity member)) {
            throw new BadRequestError("Only members can have plans");
        }

        if (plan.equals("FREE")) {
            member.getMembership().setLevel(EMembershipLevel.FREE);
            member.getMembership().setStartDate(LocalDateTime.now());
            member.getMembership().setEndDate(null);
        } else if (plan.equals("PREMIUM")) {
            member.getMembership().setLevel(EMembershipLevel.PREMIUM);
            member.getMembership().setStartDate(LocalDateTime.now());
            member.getMembership().setEndDate(LocalDateTime.now().plusMonths(1)); // 1 tháng
        } else {
            throw new BadRequestError("Invalid plan: " + plan);
        }

        userRepository.save(member);
        return true;
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
    public UserEntity updateUserByID(UUID id, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        getUpdateStrategy(user).update(user, request);
        return userRepository.save(user);
    }

    @Override
    public boolean changePasswordById(UUID id, String newPassword) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        if (newPassword.length() < 6) {
            throw new BadRequestError("Password must be at least 6 characters long");
        }

        if (newPassword.equals(user.getPassword())) {
            throw new BadRequestError("New password cannot be the same as the old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
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

    @Override
    public void activateUser(UUID id) {
        // Soft delete user by setting isActive to false
        Optional<UserEntity> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new NotFoundError("User not found with id: " + id);
        }
        UserEntity user = optUser.get();
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public int getTotalUsers() {
        return (int) userRepository.count();
    }

    @Override
    public int getTotalMembers() {
        return userRepository.findAll().stream()
                .filter(user -> user instanceof MemberEntity)
                .mapToInt(user -> 1)
                .sum();
    }

    @Override
    public int getInactiveMembers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isActive())
                .mapToInt(user -> 1)
                .sum();
    }

    @Override
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
        return followCourseRepository.existsByFollower_UserId(memberId);
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return userRepository.findById(id);
    }
}