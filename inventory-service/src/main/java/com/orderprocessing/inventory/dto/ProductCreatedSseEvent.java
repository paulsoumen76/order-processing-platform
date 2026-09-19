package com.orderprocessing.inventory.dto;

public record ProductCreatedSseEvent(
        String productId,
        String productName
) {
}