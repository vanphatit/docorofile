package com.group.docorofile.observer;

import com.group.docorofile.request.CreateNotificationRequest;

import java.util.ArrayList;
import java.util.List;

public class NotificationCenter {
    private static final List<NotificationObserver> observers = new ArrayList<>();

    public static void registerObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public static void notifyObservers(CreateNotificationRequest request) {
        for (NotificationObserver observer : observers) {
            observer.notify(request);
        }
    }
}
