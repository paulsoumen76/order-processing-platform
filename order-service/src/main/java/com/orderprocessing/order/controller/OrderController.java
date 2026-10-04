package com.orderprocessing.order.controller;

import com.orderprocessing.order.dto.CreateOrderRequest;
import com.orderprocessing.order.model.Order;
import com.orderprocessing.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "orderId", order.getOrderId(),
                        "status", order.getStatus().name(),
                        "productId", order.getProductId(),
                        "quantity", order.getQuantity(),
                        "amount", order.getAmount(),
                        "notificationEmail", request.notificationEmail(),
                        "createdAt", order.getCreatedAt()
                )
        );
    }
}
