package com.orderprocessing.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.notification.event.ProductSavedEvent;
import com.orderprocessing.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductNotificationListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;


    public ProductNotificationListener(NotificationService notificationService, ObjectMapper objectMapper) {

        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "inventory.product.saved",
            groupId = "notification-service"
    )
    public void handleProductSaved(String message) throws Exception {

        ProductSavedEvent event =
                objectMapper.readValue(message, ProductSavedEvent.class);

        System.out.println("[Notification] Event received: " + message);

        notificationService.processProductSavedEvent(event);
    }
}