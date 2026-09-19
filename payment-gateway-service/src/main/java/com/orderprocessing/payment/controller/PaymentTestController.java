package com.orderprocessing.payment.controller;

import com.orderprocessing.payment.model.Payment;
import com.orderprocessing.payment.model.PaymentStatus;
import com.orderprocessing.payment.repository.PaymentRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/test/payments")
public class PaymentTestController {

    private final PaymentRepository paymentRepository;

    public PaymentTestController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public Payment createTestPayment(
            @RequestParam String orderId,
            @RequestParam BigDecimal amount
    ) {

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setCurrency("INR");
        payment.setStatus(PaymentStatus.CREATED);

        return paymentRepository.save(payment);
    }
}