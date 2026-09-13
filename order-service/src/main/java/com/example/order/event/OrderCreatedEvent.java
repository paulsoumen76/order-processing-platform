package com.example.order.event;

public record OrderCreatedEvent(
        String orderId,
        String productId,
        int quantity
) {}
