package com.example.order;

import com.example.order.api.CreateOrderRequest;
import com.example.order.event.OrderCreatedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        if (request.productId() == null || request.productId().isBlank() || request.quantity() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "productId and positive quantity are required"));
        }

        String orderId = UUID.randomUUID().toString();
        OrderCreatedEvent event =
                new OrderCreatedEvent(orderId, request.productId(), request.quantity());

        kafkaTemplate.send("order.created", orderId, event);

        return ResponseEntity.accepted().body(Map.of(
                "orderId", orderId,
                "status", "ORDER_CREATED"
        ));
    }
}
