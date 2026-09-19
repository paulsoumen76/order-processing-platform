package com.orderprocessing.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.notification.event.InventoryReservedEvent;
import com.orderprocessing.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservedNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public InventoryReservedNotificationConsumer(
            ObjectMapper objectMapper,
            NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "inventory.reserved",
            groupId = "notification-service"
    )
    public void consume(String message) throws Exception {

        System.out.println(
                "[Notification] Raw inventory.reserved event: " + message
        );

        InventoryReservedEvent event =
                objectMapper.readValue(
                        message,
                        InventoryReservedEvent.class
                );

        notificationService.processInventoryReservedEvent(event);
    }
}