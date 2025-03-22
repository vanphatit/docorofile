package com.group.docorofile.models.course;

import com.group.docorofile.entities.CourseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseDetailResponseDTO {
    private String courseName;
    private String description;
    private String universityName;

    // Constructor để map từ CourseEntity
    public CourseDetailResponseDTO(CourseEntity course) {
        this.courseName = course.getCourseName();
        this.description = course.getDescription();
        this.universityName = course.getUniversity().getUnivName();
    }
}
