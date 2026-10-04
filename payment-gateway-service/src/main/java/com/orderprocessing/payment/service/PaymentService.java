package com.orderprocessing.payment.service;

import com.orderprocessing.payment.client.RazorpayClient;
import com.orderprocessing.payment.config.RazorpayProperties;
import com.orderprocessing.payment.dto.PaymentResponse;
import com.orderprocessing.payment.dto.VerifyPaymentRequest;
import com.orderprocessing.payment.event.InventoryReservedEvent;
import com.orderprocessing.payment.model.Payment;
import com.orderprocessing.payment.model.PaymentStatus;
import com.orderprocessing.payment.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;

    public PaymentService(PaymentRepository paymentRepository, RazorpayClient razorpayClient, RazorpayProperties razorpayProperties) {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
        this.razorpayProperties = razorpayProperties;
    }

    @Transactional
    public void createPayment(InventoryReservedEvent event) {

        if (!event.reserved()) {

            System.out.println(
                    "[Payment] Inventory was not reserved. " +
                            "Skipping payment for order: " +
                            event.orderId()
            );

            return;
        }

        if (paymentRepository.existsByOrderId(event.orderId())) {

            System.out.println(
                    "[Payment] Payment already exists for order: "
                            + event.orderId()
            );

            return;
        }

        Payment payment = new Payment();

        payment.setOrderId(event.orderId());
        payment.setAmount(event.amount());
        payment.setCurrency("INR");
        payment.setStatus(PaymentStatus.CREATED);

        paymentRepository.save(payment);

        RazorpayClient.RazorpayOrderResponse razorpayOrder =
                razorpayClient.createOrder(
                        event.orderId(),
                        event.amount(),
                        "INR"
                );

        payment.setRazorpayOrderId(razorpayOrder.id());

        paymentRepository.save(payment);

        System.out.println(
                "[Payment] Razorpay order created. " +
                        "Order: " + event.orderId() +
                        ", Razorpay Order: " + razorpayOrder.id() +
                        ", Amount: ₹" + event.amount()
        );
    }

    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {

        Payment payment = paymentRepository
                .findByOrderId(request.orderId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found for order"));

        if (!payment.getRazorpayOrderId().equals(request.razorpayOrderId())) {
            throw new IllegalArgumentException("Razorpay order ID does not match");
        }

        String payload =
                request.razorpayOrderId()
                        + "|"
                        + request.razorpayPaymentId();

        String expectedSignature = generateSignature(
                payload,
                razorpayProperties.keySecret()
        );

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                request.razorpaySignature().getBytes(StandardCharsets.UTF_8))) {

            throw new IllegalArgumentException("Invalid Razorpay signature");
        }

        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    private String generateSignature(String payload, String secret) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(
                            secret.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );

            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Unable to generate Razorpay signature",
                    e
            );
        }
    }

    public Optional<PaymentResponse> getPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::toResponse);

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