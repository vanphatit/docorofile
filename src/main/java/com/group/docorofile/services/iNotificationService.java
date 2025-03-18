package com.group.docorofile.services;

import com.group.docorofile.enums.ENotificationType;
import com.group.docorofile.models.dto.NotificationDTO;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface iNotificationService {
    NotificationDTO createNotification(UUID receiverId, ENotificationType type, String message);

    ResultPaginationDTO fetchNotifications(String email, Pageable pageable);

    void markAsRead(UUID id);
}
