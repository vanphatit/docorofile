package com.group.docorofile.services.impl;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.models.course.CourseDTO;
import com.group.docorofile.repositories.CourseRepository;
import com.group.docorofile.repositories.UniversityRepository;
import com.group.docorofile.services.iCourseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements iCourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Override
    public CourseEntity createCourse(CourseDTO dto) {
        if (courseRepository.existsByCourseName(dto.getCourseName())) {
            throw new RuntimeException("Course already exists: " + dto.getCourseName());
        }

        UniversityEntity university = universityRepository.findByUnivId(dto.getUniversityId())
                .orElseThrow(() -> new EntityNotFoundException("University not found"));

        CourseEntity course = CourseEntity.builder()
                .courseName(dto.getCourseName())
                .description(dto.getDescription())
                .university(university)
                .build();

        return courseRepository.save(course);
    }

    @Override
    public List<CourseCreatedResponseDTO> getAllCoursesAsDTO() {
        return courseRepository.findAll().stream()
                .map(course -> new CourseCreatedResponseDTO(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getUniversity().getUnivName()
                ))
                .toList();
    }

    @Override
    public List<CourseEntity> searchCoursesByName(String keyword) {
        return courseRepository.findByCourseNameContaining(keyword);
    }

    @Override
    public CourseEntity findByCourseNameAndUniversityName(String courseName, String universityName) {
        return courseRepository.findByCourseNameAndUniversityName(courseName, universityName)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    @Override
    public List<CourseCreatedResponseDTO> findCourseDTOsByUniversityName(String universityName) {
        return courseRepository.findAllByUniversityName(universityName).stream()
                .map(course -> new CourseCreatedResponseDTO(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getUniversity().getUnivName()
                ))
                .toList();
    }

    @Override
    public CourseEntity updateCourse(CourseDTO dto) {
        CourseEntity course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + dto.getCourseId()));

        // Kiểm tra xem trường đại học có tồn tại không
        UniversityEntity university = universityRepository.findById(dto.getUniversityId())
                .orElseThrow(() -> new RuntimeException("University not found with ID: " + dto.getUniversityId()));

        // Cập nhật thông tin khóa học
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setUniversity(university);

        return courseRepository.save(course);
    }
}