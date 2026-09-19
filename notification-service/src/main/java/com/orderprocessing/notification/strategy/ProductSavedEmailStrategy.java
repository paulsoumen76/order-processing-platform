package com.orderprocessing.notification.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.notification.event.ProductSavedEvent;
import com.orderprocessing.notification.model.NotificationEventType;
import com.orderprocessing.notification.model.NotificationType;
import com.orderprocessing.notification.model.OutboxEvent;
import com.orderprocessing.notification.service.EmailNotificationService;
import org.springframework.stereotype.Component;

@Component
public class ProductSavedEmailStrategy
        implements NotificationStrategy {

    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    public ProductSavedEmailStrategy(
            ObjectMapper objectMapper,
            EmailNotificationService emailNotificationService) {

        this.objectMapper = objectMapper;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public NotificationEventType getEventType() {
        return NotificationEventType.PRODUCT_SAVED;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.EMAIL;
    }

    @Override
    public void send(OutboxEvent event) {

        try {
            ProductSavedEvent productSavedEvent =
                    objectMapper.readValue(
                            event.getPayload(),
                            ProductSavedEvent.class
                    );

            emailNotificationService.sendProductSavedEmail(
                    productSavedEvent
            );

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Failed to send product saved notification",
                    exception
            );
        }
    }
}