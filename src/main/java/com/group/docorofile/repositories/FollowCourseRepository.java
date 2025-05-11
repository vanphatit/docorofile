package com.group.docorofile.repositories;

import com.group.docorofile.entities.CourseEntity;
import com.group.docorofile.entities.FollowCourseEntity;
import com.group.docorofile.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowCourseRepository extends JpaRepository<FollowCourseEntity, UUID> {

    boolean existsByFollower_UserId (UUID follower);

    boolean existsByFollowerAndCourse(UserEntity follower, CourseEntity course);

    long countByFollower(UserEntity follower);

    List<FollowCourseEntity> findByFollower(UserEntity follower);

    Optional<FollowCourseEntity> findByFollowerAndCourse(UserEntity follower, CourseEntity course);

    int countByCourse_CourseId(UUID courseId);

}

