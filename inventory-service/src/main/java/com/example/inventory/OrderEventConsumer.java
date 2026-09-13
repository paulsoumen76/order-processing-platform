package com.example.inventory;

import com.example.inventory.event.InventoryReservedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventConsumer(ObjectMapper objectMapper,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order.created")
    public void consume(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);

        String orderId = event.get("orderId").asText();
        String productId = event.get("productId").asText();
        int quantity = event.get("quantity").asInt();

        // Demo inventory logic: reservation always succeeds.
        boolean reserved = quantity > 0;

        System.out.printf(
                "[Inventory] Reserving %d x %s for order %s%n",
                quantity, productId, orderId
        );

        InventoryReservedEvent result =
                new InventoryReservedEvent(orderId, productId, quantity, reserved);

        kafkaTemplate.send("inventory.reserved", orderId, result);
    }
}
