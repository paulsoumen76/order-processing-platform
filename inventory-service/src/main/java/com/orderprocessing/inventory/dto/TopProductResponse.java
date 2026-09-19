package com.orderprocessing.inventory.dto;

import java.math.BigDecimal;

public record TopProductResponse(
        String productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        String imageUrl,
        long salesCount
) {
}