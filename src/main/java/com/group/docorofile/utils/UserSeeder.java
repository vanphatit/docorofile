package com.group.docorofile.utils;


import com.group.docorofile.entities.*;
import com.group.docorofile.enums.EMembershipLevel;
import com.group.docorofile.repositories.MembershipRepository;
import com.group.docorofile.repositories.UniversityRepository;
import com.group.docorofile.repositories.UserRepository;
import com.group.docorofile.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final UserServiceImpl userService;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniversityRepository universityRepository;

    public void seedUsers() {
        UniversityEntity uit = universityRepository.findByUnivName("University of Information Technology")
                .orElseThrow(() -> new RuntimeException("University not found"));

        if (userRepository.count() == 0) {

            // ===== ADMIN =====
            AdminEntity admin1 = AdminEntity.builder()
                    .fullName("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .createdOn(LocalDateTime.of(2024, 1, 1, 10, 0))
                    .modifiedOn(LocalDateTime.of(2024, 1, 1, 10, 0))
                    .build();
            userRepository.save(admin1);

            // ===== MODERATOR =====
            ModeratorEntity moderator1 = ModeratorEntity.builder()
                    .fullName("Moderator 1")
                    .email("mod1@email.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .isReportManage(true)
                    .createdOn(LocalDateTime.of(2024, 3, 3, 11, 11))
                    .modifiedOn(LocalDateTime.of(2024, 3, 3, 11, 11))
                    .build();
            userRepository.save(moderator1);

            ModeratorEntity moderator2 = ModeratorEntity.builder()
                    .fullName("Moderator 2")
                    .email("mod2@email.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .isReportManage(true)
                    .createdOn(LocalDateTime.of(2024, 3, 3, 11, 11))
                    .modifiedOn(LocalDateTime.of(2024, 3, 3, 11, 11))
                    .build();
            userRepository.save(moderator2);

            // download limit cua premium la bao nhieu
            // ===== MEMBER 1 =====
            MembershipEntity membership1 = MembershipEntity.builder()
                    .level(EMembershipLevel.FREE)
                    .startDate(LocalDateTime.of(2024, 2, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 2, 1, 0, 0))
                    .build();


            MemberEntity member1 = MemberEntity.builder()
                    .fullName("Alice Nguyen")
                    .email("alice@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .membership(membership1)
                    .isChat(true)
                    .isComment(true)
                    .downloadLimit(10)
                    .studyAt(uit)
                    .createdOn(LocalDateTime.of(2024, 2, 2, 9, 30))
                    .modifiedOn(LocalDateTime.of(2024, 2, 2, 9, 30))
                    .build();
            userRepository.save(member1);

//            // ===== MEMBER 2 =====
            MembershipEntity membership2 = MembershipEntity.builder()
                    .level(EMembershipLevel.FREE)
                    .startDate(LocalDateTime.of(2024, 2, 2, 0, 0))
                    .endDate(LocalDateTime.of(2025, 2, 2, 0, 0))
                    .build();


            MemberEntity member2 = MemberEntity.builder()
                    .fullName("Bob Tran")
                    .email("bob@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .membership(membership2)
                    .isChat(true)
                    .isComment(false)
                    .downloadLimit(8)
                    .studyAt(uit)
                    .createdOn(LocalDateTime.of(2024, 2, 3, 10, 0))
                    .modifiedOn(LocalDateTime.of(2024, 2, 3, 10, 0))
                    .build();
            userRepository.save(member2);

            // ===== MEMBER 3 =====
            MembershipEntity membership3 = MembershipEntity.builder()
                    .level(EMembershipLevel.PREMIUM)
                    .startDate(LocalDateTime.of(2024, 2, 3, 0, 0))
                    .endDate(LocalDateTime.of(2025, 2, 3, 0, 0))
                    .build();


            MemberEntity member3 = MemberEntity.builder()
                    .fullName("Charlie Le")
                    .email("charlie@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .membership(membership3)
                    .isChat(false)
                    .isComment(true)
                    .downloadLimit(7)
                    .studyAt(uit)
                    .createdOn(LocalDateTime.of(2024, 2, 4, 9, 45))
                    .modifiedOn(LocalDateTime.of(2024, 2, 4, 9, 45))
                    .build();
            userRepository.save(member3);

            // ===== MEMBER 4 =====
            MembershipEntity membership4 = MembershipEntity.builder()
                    .level(EMembershipLevel.FREE)
                    .startDate(LocalDateTime.of(2024, 2, 3, 0, 0))
                    .endDate(LocalDateTime.of(2025, 2, 3, 0, 0))
                    .build();


            MemberEntity member4 = MemberEntity.builder()
                    .fullName("David Pham")
                    .email("david@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(false)
                    .membership(membership4)
                    .isChat(false)
                    .isComment(true)
                    .downloadLimit(7)
                    .studyAt(uit)
                    .createdOn(LocalDateTime.of(2024, 2, 4, 9, 45))
                    .modifiedOn(LocalDateTime.of(2024, 2, 4, 9, 45))
                    .build();
            userRepository.save(member4);

            // ===== MEMBER 5 =====
            MembershipEntity membership5 = MembershipEntity.builder()
                    .level(EMembershipLevel.PREMIUM)
                    .startDate(LocalDateTime.of(2024, 2, 4, 0, 0))
                    .endDate(LocalDateTime.of(2025, 2, 4, 0, 0))
                    .build();

            MemberEntity member5 = MemberEntity.builder()
                    .fullName("Emma Vo")
                    .email("emma@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .membership(membership5)
                    .isChat(true)
                    .isComment(false)
                    .downloadLimit(6)
                    .studyAt(uit)
                    .createdOn(LocalDateTime.of(2024, 2, 5, 10, 0))
                    .modifiedOn(LocalDateTime.of(2024, 2, 5, 10, 0))
                    .build();

            userRepository.save(member5);

            // ===== MEMBER 6 =====
            MembershipEntity membership6 = MembershipEntity.builder()
                    .level(EMembershipLevel.FREE)
                    .startDate(LocalDateTime.of(2024, 2, 5, 0, 0))
                    .endDate(LocalDateTime.of(2025, 2, 5, 0, 0))
                    .build();

            MemberEntity member6 = MemberEntity.builder()
                    .fullName("Frank Do")
                    .email("frank@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .membership(membership6)
                    .isChat(false)
                    .isComment(true)
                    .downloadLimit(8)
                    .studyAt(uit)
                    .createdOn(LocalDateTime.of(2024, 2, 6, 11, 0))
                    .modifiedOn(LocalDateTime.of(2024, 2, 6, 11, 0))
                    .build();
            userRepository.save(member6);

            // ===== MEMBER 4 =====
//            MembershipEntity membership4 = MembershipEntity.builder()
//                    .membershipId(UUID.fromString("01964240-fc03-7ab7-bbce-16bb0337a3da"))
//                    .level(EMembershipLevel.FREE)
//                    .startDate(LocalDateTime.of(2024, 2, 4, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 4, 0, 0))
//                    .build();
//
//
//            MemberEntity member4 = MemberEntity.builder()
//                    .fullName("David Pham")
//                    .email("david@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership4)
//                    .isChat(true)
//                    .isComment(false)
//                    .downloadLimit(6)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 5, 9, 0))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 5, 9, 0))
//                    .build();
//            member4.setProfileValue("firstname", "David");
//            member4.setProfileValue("lastname", "Pham");
//            member4.setProfileValue("studyat", "Ho Chi Minh City University of Technology");
//            userRepository.save(member4);
//
//            // ===== MEMBER 5 =====
//            MembershipEntity membership5 = MembershipEntity.builder()
//                    .level(EMembershipLevel.PREMIUM)
//                    .startDate(LocalDateTime.of(2024, 2, 5, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 5, 0, 0))
//                    .build();
//
//
//            MemberEntity member5 = MemberEntity.builder()
//                    .fullName("Emma Vo")
//                    .email("emma@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership5)
//                    .isChat(false)
//                    .isComment(true)
//                    .downloadLimit(9)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 6, 10, 15))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 6, 10, 15))
//                    .build();
//            member5.setProfileValue("firstname", "Emma");
//            member5.setProfileValue("lastname", "Vo");
//            member5.setProfileValue("studyat", "University of Information Technology");
//            userRepository.save(member5);
//
//            // ===== MEMBER 6 =====
//            MembershipEntity membership6 = MembershipEntity.builder()
//                    .level(EMembershipLevel.FREE)
//                    .startDate(LocalDateTime.of(2024, 2, 6, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 6, 0, 0))
//                    .build();
//
//
//            MemberEntity member6 = MemberEntity.builder()
//                    .fullName("Frank Do")
//                    .email("frank@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership6)
//                    .isChat(true)
//                    .isComment(true)
//                    .downloadLimit(10)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 7, 11, 0))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 7, 11, 0))
//                    .build();
//            member6.setProfileValue("firstname", "Frank");
//            member6.setProfileValue("lastname", "Do");
//            member6.setProfileValue("studyat", "Posts and Telecommunications Institute of Technology");
//            userRepository.save(member6);
//
//            // ===== MEMBER 7 =====
//            MembershipEntity membership7 = MembershipEntity.builder()
//                    .membershipId(UUID.fromString("01964259-1331-7e41-bd76-c4e381e006f0"))
//                    .level(EMembershipLevel.PREMIUM)
//                    .startDate(LocalDateTime.of(2024, 2, 7, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 7, 0, 0))
//                    .build();
//
//
//            MemberEntity member7 = MemberEntity.builder()
//                    .fullName("Grace Nguyen")
//                    .email("grace@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership7)
//                    .isChat(false)
//                    .isComment(true)
//                    .downloadLimit(7)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 8, 9, 30))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 8, 9, 30))
//                    .build();
//            member7.setProfileValue("firstname", "Grace");
//            member7.setProfileValue("lastname", "Nguyen");
//            member7.setProfileValue("studyat", "Ho Chi Minh City University of Technology");
//            userRepository.save(member7);
//
//            // ===== MEMBER 8 =====
//            MembershipEntity membership8 = MembershipEntity.builder()
//                    .level(EMembershipLevel.FREE)
//                    .startDate(LocalDateTime.of(2024, 2, 8, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 8, 0, 0))
//                    .build();
//
//
//            MemberEntity member8 = MemberEntity.builder()
//                    .fullName("Henry Bui")
//                    .email("henry@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership8)
//                    .isChat(true)
//                    .isComment(false)
//                    .downloadLimit(5)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 9, 8, 30))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 9, 8, 30))
//                    .build();
//            member8.setProfileValue("firstname", "Henry");
//            member8.setProfileValue("lastname", "Bui");
//            member8.setProfileValue("studyat", "University of Information Technology");
//            userRepository.save(member8);
//
//            // ===== MEMBER 9 =====
//            MembershipEntity membership9 = MembershipEntity.builder()
//                    .level(EMembershipLevel.PREMIUM)
//                    .startDate(LocalDateTime.of(2024, 2, 9, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 9, 0, 0))
//                    .build();
//
//
//            MemberEntity member9 = MemberEntity.builder()
//                    .fullName("Ivy Ha")
//                    .email("ivy@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership9)
//                    .isChat(false)
//                    .isComment(true)
//                    .downloadLimit(6)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 10, 9, 15))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 10, 9, 15))
//                    .build();
//            member9.setProfileValue("firstname", "Ivy");
//            member9.setProfileValue("lastname", "Ha");
//            member9.setProfileValue("studyat", "Posts and Telecommunications Institute of Technology");
//            userRepository.save(member9);
//
//            // ===== MEMBER 10 =====
//            MembershipEntity membership10 = MembershipEntity.builder()
//                    .level(EMembershipLevel.FREE)
//                    .startDate(LocalDateTime.of(2024, 2, 10, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 10, 0, 0))
//                    .build();
//
//
//            MemberEntity member10 = MemberEntity.builder()
//                    .fullName("Jack Dang")
//                    .email("jack@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership10)
//                    .isChat(true)
//                    .isComment(true)
//                    .downloadLimit(8)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 11, 10, 0))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 11, 10, 0))
//                    .build();
//            member10.setProfileValue("firstname", "Jack");
//            member10.setProfileValue("lastname", "Dang");
//            member10.setProfileValue("studyat", "Ho Chi Minh City University of Technology");
//            userRepository.save(member10);
//
//            // ===== MEMBER 11 =====
//            MembershipEntity membership11 = MembershipEntity.builder()
//                    .membershipId(UUID.fromString("01964259-1331-7d20-8b4f-9333bf7fadb9"))
//                    .level(EMembershipLevel.PREMIUM)
//                    .startDate(LocalDateTime.of(2024, 2, 11, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 11, 0, 0))
//                    .build();
//
//
//            MemberEntity member11 = MemberEntity.builder()
//                    .fullName("Kate Lam")
//                    .email("kate@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership11)
//                    .isChat(false)
//                    .isComment(false)
//                    .downloadLimit(7)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 12, 11, 45))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 12, 11, 45))
//                    .build();
//            member11.setProfileValue("firstname", "Kate");
//            member11.setProfileValue("lastname", "Lam");
//            member11.setProfileValue("studyat", "University of Information Technology");
//            userRepository.save(member11);
//
//            // ===== MEMBER 12 =====
//            MembershipEntity membership12 = MembershipEntity.builder()
//                    .level(EMembershipLevel.FREE)
//                    .startDate(LocalDateTime.of(2024, 2, 12, 0, 0))
//                    .endDate(LocalDateTime.of(2025, 2, 12, 0, 0))
//                    .build();
//
//
//            MemberEntity member12 = MemberEntity.builder()
//                    .fullName("Leo Nguyen")
//                    .email("leo@gmail.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .isActive(true)
//                    .membership(membership12)
//                    .isChat(true)
//                    .isComment(true)
//                    .downloadLimit(6)
//                    .myProfile(new HashMap<>())
//                    .createdOn(LocalDateTime.of(2024, 2, 13, 9, 30))
//                    .modifiedOn(LocalDateTime.of(2024, 2, 13, 9, 30))
//                    .build();
//            member12.setProfileValue("firstname", "Leo");
//            member12.setProfileValue("lastname", "Nguyen");
//            member12.setProfileValue("studyat", "Posts and Telecommunications Institute of Technology");
//            userRepository.save(member12);

            // ===== MEMBER 13 =====
            MembershipEntity membership13 = MembershipEntity.builder()
                    .level(EMembershipLevel.PREMIUM)
                    .startDate(LocalDateTime.of(2024, 2, 13, 0, 0))
                    .endDate(LocalDateTime.of(2025, 2, 13, 0, 0))
                    .build();


            MemberEntity member13 = MemberEntity.builder()
                    .fullName("Mia Tran")
                    .email("mia@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .membership(membership13)
                    .isChat(false)
                    .isComment(true)
                    .downloadLimit(9)
                    .studyAt(uit)
                    .createdOn(LocalDateTime.of(2024, 2, 14, 10, 15))
                    .modifiedOn(LocalDateTime.of(2024, 2, 14, 10, 15))
                    .build();
            userRepository.save(member13);
//
        }
    }
}
