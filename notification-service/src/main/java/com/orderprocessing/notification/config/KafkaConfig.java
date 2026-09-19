package com.orderprocessing.notification.config;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic orderCreatedDltTopic() {
        return new NewTopic(
                "order.created.DLT",
                3,
                (short) 1
        );
    }

    @Bean
    NewTopic inventoryReservedDltTopic() {
        return new NewTopic(
                "inventory.reserved.DLT",
                3,
                (short) 1
        );
    }

    @Bean
    NewTopic productSavedDltTopic() {
        return new NewTopic(
                "inventory.product.saved.DLT",
                3,
                (short) 1
        );
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        record.topic() + ".DLT",
                                        record.partition()
                                )
                );

        FixedBackOff backOff =
                new FixedBackOff(
                        2000L,
                        2L
                );

        return new DefaultErrorHandler(
                recoverer,
                backOff
        );
    }
}