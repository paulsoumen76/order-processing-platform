package com.orderprocessing.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.order.client.InventoryClient;
import com.orderprocessing.order.dto.CreateOrderRequest;
import com.orderprocessing.order.dto.ProductResponse;
import com.orderprocessing.order.event.OrderCreatedEvent;
import com.orderprocessing.order.model.Order;
import com.orderprocessing.order.model.OrderEventType;
import com.orderprocessing.order.model.OrderStatus;
import com.orderprocessing.order.model.OutboxEvent;
import com.orderprocessing.order.model.OutboxEventStatus;
import com.orderprocessing.order.repository.OrderRepository;
import com.orderprocessing.order.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final InventoryClient inventoryClient;

    public OrderService(
            OrderRepository orderRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            InventoryClient inventoryClient) {

        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        ProductResponse product =
                inventoryClient.getProduct(request.productId());

        BigDecimal amount =
                product.price()
                        .multiply(BigDecimal.valueOf(request.quantity()));

        String orderId = UUID.randomUUID().toString();

        Order order = new Order();

        order.setOrderId(orderId);
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setAmount(amount);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        UUID.randomUUID().toString(),
                        orderId,
                        request.productId(),
                        request.quantity(),
                        request.notificationEmail(),
                        amount
                );

        String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to serialize order created event",
                    e
            );
        }

        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setEventType(OrderEventType.ORDER_CREATED);
        outboxEvent.setAggregateId(orderId);
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setCreatedAt(LocalDateTime.now());

        outboxEventRepository.save(outboxEvent);

        return order;
    }
}