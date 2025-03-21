package com.group.docorofile.controllers.v1.api.course;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.models.course.CourseDTO;
import com.group.docorofile.models.course.CourseDetailResponseDTO;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.services.impl.CourseServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/api/courses")
public class CourseController {
    @Autowired
    private CourseServiceImpl courseService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<SuccessResponse> createCourse(@Valid @RequestBody CourseDTO dto) {
        CourseEntity createdCourse = courseService.createCourse(dto);

        CourseCreatedResponseDTO responseData = new CourseCreatedResponseDTO(
                createdCourse.getCourseId(),
                createdCourse.getCourseName(),
                createdCourse.getUniversity().getUnivName()
        );

        SuccessResponse response = new SuccessResponse(
                "Course created successfully",
                HttpStatus.CREATED.value(),
                responseData,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/all")
    public ResponseEntity<List<CourseCreatedResponseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCoursesAsDTO());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/by-university")
    public ResponseEntity<List<CourseCreatedResponseDTO>> getCoursesByUniversity(@RequestParam("name") String universityName) {
        List<CourseCreatedResponseDTO> courses = courseService.findCourseDTOsByUniversityName(universityName);
        return ResponseEntity.ok(courses);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/search")
    public ResponseEntity<List<CourseCreatedResponseDTO>> searchCourses(@RequestParam("keyword") String keyword) {
        List<CourseCreatedResponseDTO> courses = courseService.searchCoursesByName(keyword).stream()
                .map(course -> new CourseCreatedResponseDTO(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getUniversity().getUnivName()
                ))
                .toList();

        return ResponseEntity.ok(courses);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/detail")
    public ResponseEntity<CourseDetailResponseDTO> getCourseDetail(
            @RequestParam("courseName") String courseName,
            @RequestParam("universityName") String universityName) {

        CourseEntity course = courseService.findByCourseNameAndUniversityName(courseName, universityName);

        // Convert entity thành DTO để trả về
        CourseDetailResponseDTO response = new CourseDetailResponseDTO(course);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<SuccessResponse> updateCourse(@Valid @RequestBody CourseDTO dto) {
        CourseEntity updatedCourse = courseService.updateCourse(dto);

        SuccessResponse response = new SuccessResponse(
                "Course updated successfully",
                HttpStatus.OK.value(),
                updatedCourse,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

}