package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "follow_courses")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class FollowCourseEntity {
    @Id
    private UUID followId = UuidCreator.getTimeOrdered();
    private Date followDate;
    private Date unFollowDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseEntity course;
}