package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class CourseEntity implements Serializable {
    @Id
    private UUID courseId;

    private String courseName;
    private String description;

    @ManyToOne
    private UniversityEntity university;

    @PrePersist
    public void prePersist() {
        if (this.courseId == null) {
            this.courseId = UuidCreator.getTimeOrdered();
        }
    }
}