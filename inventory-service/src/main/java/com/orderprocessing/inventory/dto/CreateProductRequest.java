package com.orderprocessing.inventory.dto;

import com.orderprocessing.inventory.model.TrackingType;

import java.math.BigDecimal;

public record CreateProductRequest(
        String productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        TrackingType trackingType,
        String notificationEmail,
        String notificationMobile
) {
}