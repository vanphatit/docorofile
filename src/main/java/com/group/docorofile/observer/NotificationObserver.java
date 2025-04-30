package com.group.docorofile.observer;

import com.group.docorofile.request.CreateNotificationRequest;

public interface NotificationObserver {
    void notify(CreateNotificationRequest request);
}