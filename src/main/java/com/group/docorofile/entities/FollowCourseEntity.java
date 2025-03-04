package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "follow_courses")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class FollowCourseEntity {
    @Id
    private UUID followId;

    @ManyToOne(optional = false)
    private MemberEntity follower;

    @ManyToOne(optional = false)
    private CourseEntity course;

    private LocalDateTime followDate;
    private LocalDateTime unfollowDate;

    @PrePersist
    public void prePersist() {
        if (this.followId == null) {
            this.followId = UuidCreator.getTimeOrdered();
        }
        if (this.followDate == null) {
            this.followDate = LocalDateTime.now();
        }
    }
}