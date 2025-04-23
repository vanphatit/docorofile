package com.group.docorofile.utils;

import com.group.docorofile.entities.ChatRoomEntity;
import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.repositories.ChatRoomRepository;
import com.group.docorofile.repositories.CourseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatRoomSeeder {
    private final CourseRepository courseRepository;
    private final ChatRoomRepository chatRoomRepository;

    public void seedChatRooms() {
        if (chatRoomRepository.count() > 0) {
            System.out.println("Chat rooms already seeded. Skipping...");
            return;
        }

        List<CourseEntity> courses = courseRepository.findAll();

        if (courses.isEmpty()) {
            System.out.println("No courses found. Cannot seed chat rooms.");
            return;
        }

        for (CourseEntity course : courses) {
            ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                    .title("Discussion for " + course.getCourseName())
                    .course(course)
                    .createdOn(LocalDateTime.now())
                    .build();

            chatRoomRepository.save(chatRoom);
            System.out.println("Created chat room for course: " + course.getCourseName());
        }
    }
}


