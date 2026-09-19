package com.orderprocessing.order.model;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PUBLISHED
}