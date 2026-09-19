package com.orderprocessing.inventory.dto;

public record ProductInventorySummary(
        int total,
        int available,
        int reserved,
        int sold,
        int damaged
) {
}