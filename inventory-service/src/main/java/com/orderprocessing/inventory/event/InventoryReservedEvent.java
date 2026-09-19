package com.orderprocessing.inventory.event;

import java.math.BigDecimal;

public record InventoryReservedEvent(
        String eventId,
        String orderId,
        String productId,
        int quantity,
        boolean reserved,
        String notificationEmail,
        BigDecimal amount
) {}
