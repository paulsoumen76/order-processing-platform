package com.orderprocessing.notification.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.notification.event.InventoryReservedEvent;
import com.orderprocessing.notification.model.NotificationType;
import com.orderprocessing.notification.model.NotificationEventType;
import com.orderprocessing.notification.model.OutboxEvent;
import com.orderprocessing.notification.service.EmailNotificationService;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservedEmailStrategy
        implements NotificationStrategy {

    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    public InventoryReservedEmailStrategy(
            ObjectMapper objectMapper,
            EmailNotificationService emailNotificationService) {
        this.objectMapper = objectMapper;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public NotificationEventType getEventType() {
        return NotificationEventType.INVENTORY_RESERVED;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.EMAIL;
    }

    @Override
    public void send(OutboxEvent event) {

        try {
            InventoryReservedEvent inventoryEvent =
                    objectMapper.readValue(
                            event.getPayload(),
                            InventoryReservedEvent.class
                    );

            emailNotificationService.sendInventoryReservedEmail(
                    inventoryEvent
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to process Inventory Reserved email notification",
                    e
            );
        }
    }
}