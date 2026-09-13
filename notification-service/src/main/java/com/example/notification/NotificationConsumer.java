package com.example.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @KafkaListener(
            topics = {"order.created", "inventory.reserved"},
            groupId = "notification-service"
    )
    public void consume(String message) {
        System.out.println("[Notification] Event received: " + message);
    }
}
