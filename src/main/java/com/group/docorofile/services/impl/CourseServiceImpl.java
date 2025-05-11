package com.group.docorofile.services.impl;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.DocumentEntity;
import com.group.docorofile.entities.UniversityEntity;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.models.course.CourseDTO;
import com.group.docorofile.models.dto.ChatRoomRequest;
import com.group.docorofile.repositories.ChatRoomRepository;
import com.group.docorofile.repositories.CourseRepository;
import com.group.docorofile.repositories.DocumentRepository;
import com.group.docorofile.repositories.UniversityRepository;
import com.group.docorofile.response.ConflictError;
import com.group.docorofile.services.iChatService;
import com.group.docorofile.services.iCourseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseServiceImpl implements iCourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    private iChatService chatService;

    @Override
    public CourseEntity createCourse(CourseDTO dto) {
        if (courseRepository.existsByCourseName(dto.getCourseName())) {
            throw new ConflictError("Course already exists: " + dto.getCourseName());
        }

        UniversityEntity university = universityRepository.findByUnivName(dto.getUniversityName())
                .orElseThrow(() -> new EntityNotFoundException("University not found"));

        CourseEntity course = CourseEntity.builder()
                .courseName(dto.getCourseName())
                .description(dto.getDescription())
                .university(university)
                .build();

//        return courseRepository.save(course);
        CourseEntity savedCourse = courseRepository.save(course);

        // Tạo ChatRoom mặc định cho khóa học
        ChatRoomRequest chatRequest = new ChatRoomRequest();
        chatRequest.setCourseId(savedCourse.getCourseId());
        chatRequest.setTitle("Discussion for " + savedCourse.getCourseName());

        chatService.createChatRoom(chatRequest);

        return savedCourse;
    }

    @Override
    public List<CourseCreatedResponseDTO> getAllCoursesAsDTO() {
        return courseRepository.findAll().stream()
                .map(course -> new CourseCreatedResponseDTO(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getUniversity().getUnivName(),
                        course.getDescription()
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
                        course.getUniversity().getUnivName(),
                        course.getDescription()
                ))
                .toList();
    }

    @Override
    public CourseEntity updateCourse(CourseDTO dto) {
        CourseEntity course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + dto.getCourseId()));

        // Kiểm tra xem trường đại học có tồn tại không
        UniversityEntity university = universityRepository.findByUnivName(dto.getUniversityName())
                .orElseThrow(() -> new EntityNotFoundException("University not found"));

        // Cập nhật thông tin khóa học
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setUniversity(university);

        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(UUID courseId) {
        List<DocumentEntity> documents = documentRepository.findByCourse_CourseId(courseId);

        if (!documents.isEmpty()) {
            throw new ConflictError("Cannot delete course. There are still documents linked to it.");
        }

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        courseRepository.delete(course);
    }

    @Override
    public Optional<CourseEntity> findById(UUID courseId) {
        return courseRepository.findById(courseId);
    }

}