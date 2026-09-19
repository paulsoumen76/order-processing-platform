package com.orderprocessing.notification.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.notification.event.OrderCreatedEvent;
import com.orderprocessing.notification.model.NotificationType;
import com.orderprocessing.notification.model.NotificationEventType;
import com.orderprocessing.notification.model.OutboxEvent;
import com.orderprocessing.notification.service.EmailNotificationService;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEmailStrategy implements NotificationStrategy {

    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    public OrderCreatedEmailStrategy(
            ObjectMapper objectMapper,
            EmailNotificationService emailNotificationService) {
        this.objectMapper = objectMapper;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public NotificationEventType getEventType() {
        return NotificationEventType.ORDER_CREATED;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.EMAIL;
    }

    @Override
    public void send(OutboxEvent event) {

        try {
            OrderCreatedEvent orderEvent =
                    objectMapper.readValue(
                            event.getPayload(),
                            OrderCreatedEvent.class
                    );

            emailNotificationService.sendOrderCreatedEmail(
                    orderEvent
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to process OrderCreated email notification",
                    e
            );
        }
    }
}