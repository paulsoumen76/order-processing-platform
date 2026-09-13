package com.example.order.api;

public record CreateOrderRequest(
        String productId,
        int quantity
) {}
