package com.group.docorofile.controllers.v1.api.user;

import com.group.docorofile.entities.ActivityLogEntity;
import com.group.docorofile.response.NotFoundError;
import com.group.docorofile.response.SuccessResponse;
import com.group.docorofile.services.impl.ActivityLogServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/logs")
public class ActivityLogAPIController {

    @Autowired
    private ActivityLogServiceImpl activityLogService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllActivityLogs(Pageable pageable) {
        Page<ActivityLogEntity> logs = activityLogService.getAllActivityLogs(pageable);
        if(!logs.isEmpty() && logs.getTotalElements() > 0) {
            SuccessResponse response = new SuccessResponse(
                    "Activity logs retrieved successfully",
                    200,
                    logs,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(new NotFoundError("No activity logs found"));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityLogById(@PathVariable("id") UUID id) {
        ActivityLogEntity log = activityLogService.getActivityLogById(id);
        if (log != null) {
            SuccessResponse response = new SuccessResponse(
                    "Activity log retrieved successfully",
                    200,
                    log,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(new NotFoundError("Activity log not found"));
        }
    }
}