package com.group.docorofile.services.impl;

import com.group.docorofile.entities.ActivityLogEntity;
import com.group.docorofile.entities.UserEntity;
import com.group.docorofile.repositories.ActivityLogRepository;
import com.group.docorofile.services.iActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ActivityLogServiceImpl implements iActivityLogService {
    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Override
    public List<ActivityLogEntity> findByActorContainingIgnoreCase(UserEntity actor, Pageable pageable) {
        return activityLogRepository.findByActorContainingIgnoreCase(actor, pageable);
    }

    @Override
    public List<ActivityLogEntity> findByCreatedOnContainingIgnoreCase(UserEntity creator, Pageable pageable) {
        return activityLogRepository.findByCreatedOnContainingIgnoreCase(creator, pageable);
    }

    @Override
    public List<ActivityLogEntity> findByActionContainingIgnoreCase(UserEntity actor, Pageable pageable) {
        return activityLogRepository.findByActionContainingIgnoreCase(actor, pageable);
    }

    @Override
    public ActivityLogEntity createActivityLog(ActivityLogEntity activityLog) {
        // Phương thức này có thể được gọi từ các sự kiện nghiệp vụ (ví dụ: khi Moderator thực hiện thao tác)
        return activityLogRepository.save(activityLog);
    }

    @Override
    public ActivityLogEntity getActivityLogById(UUID logId) {
        Optional<ActivityLogEntity> log = activityLogRepository.findById(logId);
        return log.orElse(null);
    }

    @Override
    public Page<ActivityLogEntity> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository.findAll(pageable);
    }
}
