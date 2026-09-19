package com.orderprocessing.notification.model;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PUBLISHED
}