package com.group.docorofile.utils;
import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.repositories.CourseRepository;
import com.group.docorofile.repositories.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CourseSeeder {

    private final CourseRepository courseRepository;
    private final UniversityRepository universityRepository;


    public void seedCourses() {
        if (courseRepository.count() == 0) {
            // Tìm các trường đại học đã được seed trước đó
            Optional<UniversityEntity> hcmutOpt = universityRepository.findByUnivName("Ho Chi Minh City University of Technology");
            Optional<UniversityEntity> ptitOpt = universityRepository.findByUnivName("Posts and Telecommunications Institute of Technology");
            Optional<UniversityEntity> uitOpt = universityRepository.findByUnivName("University of Information Technology");

            // Nếu có trường thì tạo course
            hcmutOpt.ifPresent(hcmut -> {
                courseRepository.save(CourseEntity.builder()
                        .courseName("Data Structures and Algorithms")
                        .description("Learn about trees, graphs, and algorithm analysis.")
                        .university(hcmut)
                        .build());

                courseRepository.save(CourseEntity.builder()
                        .courseName("Software Engineering")
                        .description("Principles of software development and project management.")
                        .university(hcmut)
                        .build());
            });

            ptitOpt.ifPresent(ptit -> {
                courseRepository.save(CourseEntity.builder()
                        .courseName("Network Fundamentals")
                        .description("Introduction to computer networks and protocols.")
                        .university(ptit)
                        .build());

                courseRepository.save(CourseEntity.builder()
                        .courseName("Database Systems")
                        .description("Relational databases, SQL, and indexing techniques.")
                        .university(ptit)
                        .build());
            });

            uitOpt.ifPresent(uit -> {
                courseRepository.save(CourseEntity.builder()
                        .courseName("Artificial Intelligence")
                        .description("Machine learning, search algorithms, and applications.")
                        .university(uit)
                        .build());

                courseRepository.save(CourseEntity.builder()
                        .courseName("Mobile Application Development")
                        .description("Build mobile apps for Android and iOS platforms.")
                        .university(uit)
                        .build());
            });
        }
    }
}
