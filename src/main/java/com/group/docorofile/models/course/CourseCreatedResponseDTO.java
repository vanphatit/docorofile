package com.group.docorofile.models.course;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseCreatedResponseDTO {
    private UUID courseId;
    private String courseName;
    private String universityName;
}
