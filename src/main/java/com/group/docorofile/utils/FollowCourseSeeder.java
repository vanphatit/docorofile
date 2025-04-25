package com.group.docorofile.utils;


import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.FollowCourseEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.repositories.CourseRepository;
import com.group.docorofile.repositories.FollowCourseRepository;
import com.group.docorofile.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowCourseSeeder {

    private final FollowCourseRepository followCourseRepository;
    private final MemberRepository memberRepository;
    private final CourseRepository courseRepository;

    public void seedFollowCourses() {
        if (followCourseRepository.count() == 0) {
            // Example: Free mem - follow 2 courses
            Optional<MemberEntity> member1 = memberRepository.findById(UUID.fromString("01964240-fc03-7d6a-ae75-7f7fed979906")); //Bob Tran
            // Premium mem
            Optional<MemberEntity> member2 = memberRepository.findById(UUID.fromString("0196422d-fc81-7b95-8031-2a41dff4031f")); //Alice Nguyen

            Optional<CourseEntity> course1 = courseRepository.findById(UUID.fromString("0196426e-19ac-737d-9e67-fa8ceca8a4c9")); //Data Structures and Algorithms
            Optional<CourseEntity> course2 = courseRepository.findById(UUID.fromString("0196426e-19ac-79c5-8551-393ba622e918")); // Software Engineering
            Optional<CourseEntity> course3 = courseRepository.findById(UUID.fromString("0196426e-19ac-7fe4-9a8f-6abcdd116cbe")); // Network Fundamentals
            Optional<CourseEntity> course4 = courseRepository.findById(UUID.fromString("0196426e-19ac-75ca-b853-ce1a545be451")); // Database Systems
            Optional<CourseEntity> course5 = courseRepository.findById(UUID.fromString("0196426e-19ac-733f-9a0c-b3c2617784b7")); // Artificial Intelligence
            Optional<CourseEntity> course6 = courseRepository.findById(UUID.fromString("0196426e-19ac-7fb0-aff4-ff345c576d27")); // Mobile Application Development



            if (member1.isPresent() && course1.isPresent()) {
                FollowCourseEntity follow1 = FollowCourseEntity.builder()
                        .followId(UUID.fromString("01965661-8a4d-77f3-99e0-7ba1bb0d68df"))
                        .follower(member1.get())
                        .course(course1.get())
                        .followedAt(LocalDateTime.now())
                        .build();

                followCourseRepository.save(follow1);
            }

            if (member2.isPresent() && course1.isPresent()) {
                FollowCourseEntity follow2 = FollowCourseEntity.builder()
                        .followId(UUID.fromString("01965661-8a4d-7909-99b6-2b9c40d7277d"))
                        .follower(member2.get())
                        .course(course1.get())
                        .followedAt(LocalDateTime.now().minusDays(1))
                        .build();

                followCourseRepository.save(follow2);
            }

            if (member1.isPresent() && course2.isPresent()) {
                FollowCourseEntity follow3 = FollowCourseEntity.builder()
                        .followId(UUID.fromString("01965661-8a4d-7a00-9016-68e3212518c9"))
                        .follower(member1.get())
                        .course(course2.get())
                        .followedAt(LocalDateTime.now().minusDays(2))
                        .build();

                followCourseRepository.save(follow3);
            }

            if (member2.isPresent() && course2.isPresent()) {
                FollowCourseEntity follow4 = FollowCourseEntity.builder()
                        .followId(UUID.fromString("01965661-8a4d-71cc-b2da-1159142eacca"))
                        .follower(member2.get())
                        .course(course2.get())
                        .followedAt(LocalDateTime.now().minusDays(3))
                        .build();

                followCourseRepository.save(follow4);
            }
        }
    }
}
