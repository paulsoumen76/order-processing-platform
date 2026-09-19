package com.orderprocessing.payment.controller;

import com.orderprocessing.payment.dto.PaymentResponse;
import com.orderprocessing.payment.model.Payment;
import com.orderprocessing.payment.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String orderId
    ) {

        return paymentRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private PaymentResponse toResponse(Payment payment) {

        return new PaymentResponse(
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getRazorpayOrderId(),
                payment.getStatus()
        );
    }
}