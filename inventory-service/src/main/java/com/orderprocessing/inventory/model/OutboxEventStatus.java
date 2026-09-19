package com.orderprocessing.inventory.model;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PUBLISHED
}