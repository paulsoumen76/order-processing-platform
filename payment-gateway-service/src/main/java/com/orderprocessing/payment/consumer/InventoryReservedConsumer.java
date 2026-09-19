package com.orderprocessing.payment.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.payment.event.InventoryReservedEvent;
import com.orderprocessing.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservedConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public InventoryReservedConsumer(
            ObjectMapper objectMapper,
            PaymentService paymentService) {

        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = "inventory.reserved",
            groupId = "payment-gateway-service"
    )
    public void consume(String message) throws Exception {

        InventoryReservedEvent event =
                objectMapper.readValue(
                        message,
                        InventoryReservedEvent.class
                );

        System.out.println(
                "[Payment] Inventory reserved event received. " +
                "Order ID: " + event.orderId() +
                ", Amount: " + event.amount() +
                ", Reserved: " + event.reserved()
        );

        paymentService.createPayment(event);
    }
}