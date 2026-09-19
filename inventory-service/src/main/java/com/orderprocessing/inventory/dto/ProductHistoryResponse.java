package com.orderprocessing.inventory.dto;

import com.orderprocessing.inventory.model.ProductHistoryAction;

import java.time.LocalDateTime;

public record ProductHistoryResponse(
        ProductHistoryAction action,
        String fieldName,
        String oldValue,
        String newValue,
        String changedBy,
        LocalDateTime changedAt
) {
}