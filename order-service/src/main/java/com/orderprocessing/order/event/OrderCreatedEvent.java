package com.orderprocessing.order.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        String eventId,
        String orderId,
        String productId,
        int quantity,
        String notificationEmail,
        BigDecimal amount
) {
}