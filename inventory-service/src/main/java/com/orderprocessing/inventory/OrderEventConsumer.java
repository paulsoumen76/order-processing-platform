package com.orderprocessing.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.inventory.idempotency.IdempotencyService;
import com.orderprocessing.inventory.service.InventoryReservationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class OrderEventConsumer {

    private static final String CONSUMER =
            "inventory-order-created-consumer";

    private final ObjectMapper objectMapper;
    private final InventoryReservationService inventoryReservationService;
    private final IdempotencyService idempotencyService;

    public OrderEventConsumer(
            ObjectMapper objectMapper,
            InventoryReservationService inventoryReservationService,
            IdempotencyService idempotencyService) {

        this.objectMapper = objectMapper;
        this.inventoryReservationService = inventoryReservationService;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    @KafkaListener(
            topics = "order.created",
            groupId = "inventory-service"
    )
    public void consume(String message) throws Exception {

        JsonNode event = objectMapper.readTree(message);

        if (event == null
                || event.get("eventId") == null
                || event.get("orderId") == null
                || event.get("productId") == null
                || event.get("quantity") == null
                || event.get("amount") == null){

            System.out.println(
                    "[Inventory] Invalid order event: " + message
            );

            return;
        }

        String eventId = event.get("eventId").asText();
        String orderId = event.get("orderId").asText();
        String productId = event.get("productId").asText();
        int quantity = event.get("quantity").asInt();
        BigDecimal amount = event.get("amount").decimalValue();
        String notificationEmail = event.get("notificationEmail").asText();

        boolean firstProcessing =
                idempotencyService.tryProcess(
                        eventId,
                        CONSUMER
                );

        if (!firstProcessing) {

            System.out.println(
                    "[Inventory] Duplicate event ignored: "
                            + eventId
            );

            return;
        }

        System.out.printf(
                "[Inventory] Reserving %d x %s for order %s%n",
                quantity,
                productId,
                orderId
        );

        inventoryReservationService.reserve(
                orderId,
                productId,
                quantity,
                notificationEmail,
                amount

        );

    }
}