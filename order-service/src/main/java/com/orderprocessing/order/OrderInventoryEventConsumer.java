package com.orderprocessing.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.order.idempotency.IdempotencyService;
import com.orderprocessing.order.model.Order;
import com.orderprocessing.order.model.OrderStatus;
import com.orderprocessing.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderInventoryEventConsumer {

    private static final String CONSUMER =
            "order-inventory-reserved-consumer";

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final IdempotencyService idempotencyService;

    public OrderInventoryEventConsumer(
            ObjectMapper objectMapper,
            OrderRepository orderRepository,
            IdempotencyService idempotencyService) {

        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    @KafkaListener(
            topics = "inventory.reserved",
            groupId = "order-service"
    )
    public void consume(String message) throws Exception {

        System.out.println(
                "[Order] Raw inventory.reserved event: " + message
        );

        JsonNode event = objectMapper.readTree(message);

        if (event == null
                || event.get("eventId") == null
                || event.get("orderId") == null
                || event.get("productId") == null
                || event.get("quantity") == null
                || event.get("reserved") == null) {

            System.out.println(
                    "[Order] Invalid inventory.reserved event: "
                            + message
            );

            return;
        }

        String eventId = event.get("eventId").asText();
        String orderId = event.get("orderId").asText();
        String productId = event.get("productId").asText();
        int quantity = event.get("quantity").asInt();
        boolean reserved = event.get("reserved").asBoolean();

        boolean firstProcessing =
                idempotencyService.tryProcess(
                        eventId,
                        CONSUMER
                );

        if (!firstProcessing) {

            System.out.println(
                    "[Order] Duplicate event ignored: "
                            + eventId
            );

            return;
        }
        Order order = orderRepository
                .findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found: " + orderId
                        ));

        if (reserved) {
            order.setStatus(OrderStatus.INVENTORY_RESERVED);
        } else {
            order.setStatus(OrderStatus.FAILED);
        }

        orderRepository.save(order);

        System.out.printf(
                "[Order] Inventory result: order=%s, product=%s, quantity=%d, reserved=%s%n",
                orderId,
                productId,
                quantity,
                reserved
        );
    }

}