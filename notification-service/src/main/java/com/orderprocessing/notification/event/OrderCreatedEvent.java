package com.orderprocessing.notification.event;

public record OrderCreatedEvent(
        String eventId,
        String orderId,
        String productId,
        int quantity,
        String notificationEmail
) {}