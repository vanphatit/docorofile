package com.group.docorofile.observer;

import com.group.docorofile.enums.ENotificationType;
import com.group.docorofile.request.CreateNotificationRequest;
import com.group.docorofile.services.iNotificationService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WebNotificationObserver implements NotificationObserver {

    private final iNotificationService notificationService;

    public WebNotificationObserver(iNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        NotificationCenter.registerObserver(this);
    }

    @Override
    public void notify(CreateNotificationRequest request) {
        notificationService.notifyFromSystem(request);
    }
}