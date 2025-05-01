package com.group.docorofile.utils;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UniversitySeeder universitySeeder;
    private final CourseSeeder courseSeeder;
    private final UserSeeder userSeeder;
    private final FollowCourseSeeder followCourseSeeder;
    private final ChatRoomSeeder chatRoomSeeder;
    private final PaymentSeeder paymentSeeder;

    @PostConstruct
    public void seedAll() {
        System.out.println("🌱 Seeding database...");
        universitySeeder.seedUniversities();
        courseSeeder.seedCourses();
        userSeeder.seedUsers();
//        followCourseSeeder.seedFollowCourses();
        chatRoomSeeder.seedChatRooms();
//        paymentSeeder.seedPayments();
        System.out.println("✅ Database seeded successfully!");
    }
}
