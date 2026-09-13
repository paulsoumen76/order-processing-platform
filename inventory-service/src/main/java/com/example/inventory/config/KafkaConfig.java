package com.example.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    NewTopic inventoryReservedTopic() {
        return new NewTopic("inventory.reserved", 1, (short) 1);
    }
}
