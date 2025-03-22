package com.group.docorofile.repositories;

import com.group.docorofile.entities.FollowCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FollowCourseRepository extends JpaRepository<FollowCourseEntity, UUID> {

    @Query("SELECT f FROM FollowCourseEntity f WHERE f.follower = :follower")
    boolean existsByFollower (UUID follower);
}
