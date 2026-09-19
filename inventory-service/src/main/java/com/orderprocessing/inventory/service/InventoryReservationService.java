package com.orderprocessing.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.inventory.event.InventoryReservedEvent;
import com.orderprocessing.inventory.exception.InsufficientSerializedInventoryException;
import com.orderprocessing.inventory.exception.InvalidTrackingTypeException;
import com.orderprocessing.inventory.exception.ProductNotFoundException;
import com.orderprocessing.inventory.model.OutboxEvent;
import com.orderprocessing.inventory.model.OutboxEventStatus;
import com.orderprocessing.inventory.model.Product;
import com.orderprocessing.inventory.repository.OutboxEventRepository;
import com.orderprocessing.inventory.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryReservationService {

    private final ProductRepository productRepository;
    private final List<InventoryReservationStrategy> strategies;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public InventoryReservationService(
            ProductRepository productRepository,
            List<InventoryReservationStrategy> strategies, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {

        this.productRepository = productRepository;
        this.strategies = strategies;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void reserve(
            String orderId,
            String productId,
            int quantity,
            String notificationEmail,
            BigDecimal amount) {
        try {
            Product product = productRepository.findByProductId(productId)
                    .orElseThrow(() ->
                            new ProductNotFoundException(productId));

            InventoryReservationStrategy strategy =
                    strategies.stream()
                            .filter(s ->
                                    s.supports(product.getTrackingType()))
                            .findFirst()
                            .orElseThrow(() ->
                                    new InvalidTrackingTypeException(productId));

            boolean reserved = strategy.reserve(productId, quantity);

            saveOutboxEvent(
                    orderId,
                    productId,
                    quantity,
                    notificationEmail,
                    reserved,
                    amount
            );
        } catch (ProductNotFoundException | InvalidTrackingTypeException | InsufficientSerializedInventoryException e) {
            saveOutboxEvent(
                    orderId,
                    productId,
                    quantity,
                    notificationEmail,
                    false,
                    amount
            );

        }
    }

    private void saveOutboxEvent(
            String orderId,
            String productId,
            int quantity,
            String notificationEmail,
            boolean reserved,
            BigDecimal amount) {

        InventoryReservedEvent event =
                new InventoryReservedEvent(
                        UUID.randomUUID().toString(),
                        orderId,
                        productId,
                        quantity,
                        reserved,
                        notificationEmail,
                        amount
                );

        String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to serialize inventory reserved event",
                    e
            );
        }

        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setEventType("InventoryReservedEvent");
        outboxEvent.setAggregateId(orderId);
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setCreatedAt(LocalDateTime.now());

        outboxEventRepository.save(outboxEvent);
    }

}