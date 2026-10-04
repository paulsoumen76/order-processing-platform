package com.orderprocessing.inventory.dto;

import com.orderprocessing.inventory.dto.ProductInventorySummary;
import com.orderprocessing.inventory.model.TrackingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        String productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        TrackingType trackingType,
        String notificationEmail,
        String notificationMobile,
        String imageUrl,
        LocalDateTime createdAt,
        ProductInventorySummary inventorySummary
) {
}