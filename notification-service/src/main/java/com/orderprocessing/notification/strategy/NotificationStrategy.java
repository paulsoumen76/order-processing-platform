package com.orderprocessing.notification.strategy;

import com.orderprocessing.notification.model.NotificationEventType;
import com.orderprocessing.notification.model.NotificationType;
import com.orderprocessing.notification.model.OutboxEvent;

public interface NotificationStrategy {

    NotificationEventType getEventType();

    NotificationType getNotificationType();

    void send(OutboxEvent event);
}