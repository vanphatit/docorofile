package com.group.docorofile.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "follow_courses")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class FollowCourseEntity implements Serializable {
    @Id
    private UUID followId;

    @ManyToOne(optional = false)
    private MemberEntity follower;

    @ManyToOne(optional = false)
    private CourseEntity course;

    @Column(name = "followed_at")
    private LocalDateTime followedAt;

    @PrePersist
    public void prePersist() {
        if (this.followId == null) {
            this.followId = UuidCreator.getTimeOrdered();
        }
    }
}