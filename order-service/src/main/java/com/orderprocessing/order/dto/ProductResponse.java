package com.orderprocessing.order.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String productId,
        String productName,
        BigDecimal price,
        Integer quantity
) {
}