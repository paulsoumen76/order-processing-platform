package com.orderprocessing.payment.controller;

import com.orderprocessing.payment.dto.PaymentResponse;
import com.orderprocessing.payment.dto.VerifyPaymentRequest;
import com.orderprocessing.payment.model.Payment;
import com.orderprocessing.payment.repository.PaymentRepository;
import com.orderprocessing.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;


    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String orderId
    ) {
        return paymentService.getPayment(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping("/verify")
    public PaymentResponse verifyPayment(
            @RequestBody VerifyPaymentRequest request) {

        return paymentService.verifyPayment(request);
    }
}