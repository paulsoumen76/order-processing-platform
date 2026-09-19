package com.orderprocessing.payment.dto;

import com.orderprocessing.payment.model.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        String orderId,
        BigDecimal amount,
        String currency,
        String razorpayOrderId,
        PaymentStatus status
) {
}