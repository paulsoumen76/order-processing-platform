package com.orderprocessing.notification.dto;

import com.orderprocessing.notification.model.NotificationStatus;
import com.orderprocessing.notification.model.NotificationType;

import java.time.LocalDateTime;

public record ProductNotificationResponse(
        Long id,
        String productId,
        NotificationType notificationType,
        String recipient,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        String failureReason
) {
}