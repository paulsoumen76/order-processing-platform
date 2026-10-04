package com.orderprocessing.payment.dto;

public record VerifyPaymentRequest(
        String orderId,
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature
) {
}