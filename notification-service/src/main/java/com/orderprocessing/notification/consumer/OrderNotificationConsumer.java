package com.orderprocessing.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.notification.event.OrderCreatedEvent;
import com.orderprocessing.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public OrderNotificationConsumer(
            ObjectMapper objectMapper,
            NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "order.created",
            groupId = "notification-service"
    )
    public void consume(String message) throws Exception {

        System.out.println(
                "[Notification] Raw order.created event: " + message
        );

        OrderCreatedEvent event =
                objectMapper.readValue(
                        message,
                        OrderCreatedEvent.class
                );

        notificationService.processOrderCreatedEvent(event);
    }
}