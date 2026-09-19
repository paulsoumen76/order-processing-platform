package com.orderprocessing.inventory.dto;

import com.orderprocessing.inventory.model.InventoryUnitStatus;

import java.time.LocalDateTime;

public record ProductUnitResponse(
        String productId,
        String productName,
        String serialNumber,
        String inventoryCode,
        InventoryUnitStatus status,
        LocalDateTime createdAt
) {}