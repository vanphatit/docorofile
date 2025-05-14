package com.group.docorofile.repositories;

import com.group.docorofile.entities.ActivityLogEntity;
import com.group.docorofile.entities.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, UUID> {

    List<ActivityLogEntity> findByActorContainingIgnoreCase(UserEntity actor, Pageable pageable);
    List<ActivityLogEntity> findByActionContainingIgnoreCase(UserEntity actor, Pageable pageable);
    List<ActivityLogEntity> findByCreatedOnContainingIgnoreCase(UserEntity creator, Pageable pageable);
}