package com.group.docorofile.services;

import com.group.docorofile.entities.ActivityLogEntity;
import com.group.docorofile.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface iActivityLogService {
    List<ActivityLogEntity> findByActorContainingIgnoreCase(UserEntity actor, Pageable pageable);

    List<ActivityLogEntity> findByCreatedOnContainingIgnoreCase(UserEntity creator, Pageable pageable);

    List<ActivityLogEntity> findByActionContainingIgnoreCase(UserEntity actor, Pageable pageable);

    ActivityLogEntity createActivityLog(ActivityLogEntity activityLog);

    ActivityLogEntity getActivityLogById(UUID logId);

    Page<ActivityLogEntity> getAllActivityLogs(Pageable pageable);
}
