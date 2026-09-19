package com.orderprocessing.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.notification.dto.ProductNotificationResponse;
import com.orderprocessing.notification.event.InventoryReservedEvent;
import com.orderprocessing.notification.event.OrderCreatedEvent;
import com.orderprocessing.notification.event.ProductSavedEvent;
import com.orderprocessing.notification.idempotency.IdempotencyService;
import com.orderprocessing.notification.model.*;
import com.orderprocessing.notification.repository.OutboxEventRepository;
import com.orderprocessing.notification.repository.ProductNotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final String CONSUMER = "notification-product-saved-consumer";
    private static final String ORDER_CREATED_CONSUMER = "notification-order-created-consumer";
    private static final String INVENTORY_RESERVED_CONSUMER = "notification-inventory-reserved-consumer";
    private final ProductNotificationRepository repository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;

    public NotificationService(
            ProductNotificationRepository repository,
            OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper, IdempotencyService idempotencyService) {

        this.repository = repository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public void processProductSavedEvent(ProductSavedEvent event) {

        boolean firstProcessing =
                idempotencyService.tryProcess(
                        event.eventId(),
                        CONSUMER
                );

        if (!firstProcessing) {
            System.out.println(
                    "[Notification] Duplicate ProductSavedEvent ignored: "
                            + event.eventId()
            );
            return;
        }

        ProductNotification notification =
                new ProductNotification();

        notification.setProductId(event.productId());
        notification.setNotificationType(NotificationType.EMAIL);
        notification.setRecipient(event.notificationEmail());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        repository.save(notification);

        String payload;

        try {
            payload =
                    objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException exception) {

            throw new RuntimeException(
                    "Failed to serialize email notification event",
                    exception
            );
        }

        OutboxEvent outboxEvent =
                new OutboxEvent();

        outboxEvent.setEventType(NotificationEventType.PRODUCT_SAVED);
        outboxEvent.setNotificationType(NotificationType.EMAIL);
        outboxEvent.setAggregateId(event.productId());
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setCreatedAt(LocalDateTime.now());

        outboxEventRepository.save(outboxEvent);
    }

    public List<ProductNotificationResponse> getNotifications(
            String productId) {

        return repository.findByProductId(productId)
                .stream()
                .map(notification ->
                        new ProductNotificationResponse(
                                notification.getId(),
                                notification.getProductId(),
                                notification.getNotificationType(),
                                notification.getRecipient(),
                                notification.getStatus(),
                                notification.getCreatedAt(),
                                notification.getSentAt(),
                                notification.getFailureReason()
                        )
                )
                .toList();
    }

    @Transactional
    public void processOrderCreatedEvent(OrderCreatedEvent event) {

        boolean firstProcessing =
                idempotencyService.tryProcess(
                        event.eventId(),
                        ORDER_CREATED_CONSUMER
                );

        if (!firstProcessing) {
            System.out.println(
                    "[Notification] Duplicate OrderCreatedEvent ignored: "
                            + event.eventId()
            );
            return;
        }

        ProductNotification notification =
                new ProductNotification();

        notification.setProductId(event.productId());
        notification.setNotificationType(NotificationType.EMAIL);
        notification.setRecipient("admin@example.com");
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        repository.save(notification);

        String payload;

        try {
            payload = objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException exception) {

            throw new RuntimeException(
                    "Failed to serialize order created notification event",
                    exception
            );
        }

        OutboxEvent outboxEvent =
                new OutboxEvent();

        outboxEvent.setEventType(
                NotificationEventType.ORDER_CREATED
        );
        outboxEvent.setNotificationType(
                NotificationType.EMAIL
        );
        outboxEvent.setAggregateId(
                event.orderId()
        );
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus(
                OutboxEventStatus.PENDING
        );
        outboxEvent.setCreatedAt(
                LocalDateTime.now()
        );

        outboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public void processInventoryReservedEvent(
            InventoryReservedEvent event) {

        boolean firstProcessing =
                idempotencyService.tryProcess(
                        event.eventId(),
                        INVENTORY_RESERVED_CONSUMER
                );

        if (!firstProcessing) {
            System.out.println(
                    "[Notification] Duplicate InventoryReservedEvent ignored: "
                            + event.eventId()
            );
            return;
        }

        ProductNotification notification =
                new ProductNotification();

        notification.setProductId(event.productId());
        notification.setNotificationType(NotificationType.EMAIL);
        notification.setRecipient("admin@example.com");
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        repository.save(notification);

        String payload;

        try {
            payload =
                    objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException exception) {

            throw new RuntimeException(
                    "Failed to serialize inventory reserved notification event",
                    exception
            );
        }

        OutboxEvent outboxEvent =
                new OutboxEvent();

        outboxEvent.setEventType(
                NotificationEventType.INVENTORY_RESERVED
        );
        outboxEvent.setNotificationType(
                NotificationType.EMAIL
        );
        outboxEvent.setAggregateId(
                event.orderId()
        );
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus(
                OutboxEventStatus.PENDING
        );
        outboxEvent.setCreatedAt(
                LocalDateTime.now()
        );

        outboxEventRepository.save(outboxEvent);
    }
}