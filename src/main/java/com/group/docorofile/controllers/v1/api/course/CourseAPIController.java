package com.group.docorofile.controllers.v1.api.course;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.models.course.CourseCreatedResponseDTO;
import com.group.docorofile.models.course.CourseDTO;
import com.group.docorofile.models.course.CourseDetailResponseDTO;
import com.group.docorofile.response.CreatedResponse;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.services.iCourseService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/courses")
public class CourseAPIController {
    @Autowired
    private iCourseService courseService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<CreatedResponse> createCourse(@Valid @RequestBody CourseDTO dto) {
        System.out.println("🔍 Received universityId = " + dto.getUniversityName());
        CourseEntity createdCourse = courseService.createCourse(dto);

        CourseCreatedResponseDTO responseData = new CourseCreatedResponseDTO(
                createdCourse.getCourseId(),
                createdCourse.getCourseName(),
                createdCourse.getUniversity().getUnivName(),
                createdCourse.getDescription()
        );

        CreatedResponse response = new CreatedResponse(
                "Course created successfully",
                responseData
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/all")
    public ResponseEntity<SuccessResponse> getAllCourses() {
        List<CourseCreatedResponseDTO> courses = courseService.getAllCoursesAsDTO();

        SuccessResponse response = new SuccessResponse(
                "All courses retrieved successfully",
                HttpStatus.OK.value(),
                courses,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/by-university")
    public ResponseEntity<SuccessResponse> getCoursesByUniversity(@RequestParam("name") String universityName) {
        List<CourseCreatedResponseDTO> courses = courseService.findCourseDTOsByUniversityName(universityName);

        SuccessResponse response = new SuccessResponse(
                "Courses retrieved by university successfully",
                HttpStatus.OK.value(),
                courses,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse> searchCourses(@RequestParam("keyword") String keyword) {
        List<CourseCreatedResponseDTO> courses = courseService.searchCoursesByName(keyword).stream()
                .map(course -> new CourseCreatedResponseDTO(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getUniversity().getUnivName(),
                        course.getDescription()
                ))
                .toList();

        SuccessResponse response = new SuccessResponse(
                "Courses searched successfully",
                HttpStatus.OK.value(),
                courses,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR','ROLE_MEMBER')")
    @GetMapping("/detail")
    public ResponseEntity<SuccessResponse> getCourseDetail(
            @RequestParam("courseName") String courseName,
            @RequestParam("universityName") String universityName) {

        CourseEntity course = courseService.findByCourseNameAndUniversityName(courseName, universityName);
        CourseDetailResponseDTO responseDTO = new CourseDetailResponseDTO(course);

        SuccessResponse response = new SuccessResponse(
                "Course detail retrieved successfully",
                HttpStatus.OK.value(),
                responseDTO,
                LocalDateTime.now()
        );

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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{courseId}")
    public ResponseEntity<SuccessResponse> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);

        SuccessResponse response = new SuccessResponse(
                "Course deleted successfully",
                HttpStatus.OK.value(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

}