package com.example.inventory.event;

public record InventoryReservedEvent(
        String orderId,
        String productId,
        int quantity,
        boolean reserved
) {}
