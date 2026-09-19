package com.orderprocessing.notification.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductSavedEvent(
        String eventId,
        String productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        String notificationEmail,
        String notificationMobile,
        LocalDateTime createdAt
) {}