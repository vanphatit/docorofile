package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CourseEntity {
    @Id
    private UUID courseId = UuidCreator.getTimeOrdered();
    private String courseName;
    private String description;

    @ManyToOne
    @JoinColumn(name = "uni_id")
    private UniversityEntity university;
}