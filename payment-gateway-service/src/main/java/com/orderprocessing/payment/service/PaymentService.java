package com.orderprocessing.payment.service;

import com.orderprocessing.payment.client.RazorpayClient;
import com.orderprocessing.payment.event.InventoryReservedEvent;
import com.orderprocessing.payment.model.Payment;
import com.orderprocessing.payment.model.PaymentStatus;
import com.orderprocessing.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    public PaymentService(PaymentRepository paymentRepository, RazorpayClient razorpayClient) {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
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
}