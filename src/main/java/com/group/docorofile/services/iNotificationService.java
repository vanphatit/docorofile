package com.group.docorofile.services;

import com.group.docorofile.enums.ENotificationType;
import com.group.docorofile.models.dto.NotificationDTO;
import com.group.docorofile.models.dto.ResultPaginationDTO;
import com.group.docorofile.request.CreateNotificationMultiRequest;
import com.group.docorofile.request.CreateNotificationRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface iNotificationService {
    void createNotification(CreateNotificationRequest request, String email);

    void notifyFromSystem(CreateNotificationRequest request);

    void sendSystemNotificationToUsers(CreateNotificationMultiRequest request, String email);

    ResultPaginationDTO fetchNotifications(String email, Pageable pageable);

    void markAsRead(UUID id);
}
