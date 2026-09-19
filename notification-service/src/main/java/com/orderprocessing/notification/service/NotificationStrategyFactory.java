package com.orderprocessing.notification.service;

import com.orderprocessing.notification.model.NotificationEventType;
import com.orderprocessing.notification.model.NotificationType;
import com.orderprocessing.notification.strategy.NotificationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationStrategyFactory {

    private final List<NotificationStrategy> strategies;

    public NotificationStrategyFactory(
            List<NotificationStrategy> strategies) {
        this.strategies = strategies;
    }

    public NotificationStrategy getStrategy(
            NotificationEventType eventType,
            NotificationType notificationType) {

        return strategies.stream()
                .filter(strategy ->
                        strategy.getEventType() == eventType
                                && strategy.getNotificationType() == notificationType
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No notification strategy found for eventType="
                                        + eventType
                                        + ", notificationType="
                                        + notificationType
                        )
                );
    }
}