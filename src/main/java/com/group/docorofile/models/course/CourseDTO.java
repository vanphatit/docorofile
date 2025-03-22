package com.group.docorofile.models.course;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private UUID courseId;
    private String courseName;
    private String description;
    private UUID universityId;
}
