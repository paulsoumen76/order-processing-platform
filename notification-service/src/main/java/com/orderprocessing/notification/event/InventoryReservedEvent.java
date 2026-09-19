package com.orderprocessing.notification.event;

public record InventoryReservedEvent(
        String eventId,
        String orderId,
        String productId,
        int quantity,
        boolean reserved,
        String notificationEmail
) {}