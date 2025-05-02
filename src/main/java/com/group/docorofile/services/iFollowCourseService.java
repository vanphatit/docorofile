package com.group.docorofile.services;

import com.group.docorofile.models.course.CourseCreatedResponseDTO;

import java.util.List;
import java.util.UUID;

public interface iFollowCourseService {
    void followCourse(UUID userId, UUID courseId);

    List<CourseCreatedResponseDTO> getFollowedCourses(UUID userId);

    void unfollowCourse(UUID userId, UUID courseId);
}
