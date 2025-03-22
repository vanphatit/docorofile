package com.group.docorofile.repositories;

import com.group.docorofile.entities.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {
    Optional<CourseEntity> findByCourseName(String courseName);
    Boolean existsByCourseName(String courseName);

    @Query("SELECT c FROM CourseEntity c WHERE c.courseName = :courseName AND c.university.univName = :universityName")
    Optional<CourseEntity> findByCourseNameAndUniversityName(String courseName, String universityName);

    @Query("SELECT f.course.courseId FROM FollowCourseEntity f WHERE f.follower.userId = :memberId")
    List<UUID> findFollowedCoursesByMemberId(UUID memberId);

    List<CourseEntity> findByCourseNameContaining(String courseName);

    @Query("SELECT c FROM CourseEntity c WHERE LOWER(c.university.univName) = LOWER(:univName)")
    List<CourseEntity> findAllByUniversityName(@Param("univName") String univName);

}
