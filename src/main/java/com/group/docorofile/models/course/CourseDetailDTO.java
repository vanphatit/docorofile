package com.group.docorofile.models.course;

import lombok.Data;

import java.util.UUID;

@Data
public class CourseDetailDTO {
    private UUID courseId;
    private String courseName;
    private String universityName;
    private int totalDocuments;
    private int totalFollowers;
}
