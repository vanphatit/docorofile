package com.group.docorofile.services;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.models.course.CourseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface iCourseService {

    CourseEntity createCourse(CourseDTO courseDTO);

    List<CourseCreatedResponseDTO> getAllCoursesAsDTO();

    List<CourseEntity> searchCoursesByName(String keyword);

    CourseEntity findByCourseNameAndUniversityName(String courseName, String universityName);

    List<CourseCreatedResponseDTO> findCourseDTOsByUniversityName(String universityName);

    CourseEntity updateCourse(CourseDTO dto);

    void deleteCourse(UUID courseId);

    Optional<CourseEntity> findById(UUID courseId);



}
