package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.model.OutboxEvent;
import com.orderprocessing.inventory.model.OutboxEventStatus;
import com.orderprocessing.inventory.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventService outboxEventService;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate, OutboxEventService outboxEventService) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventService = outboxEventService;
    }

    public List<OutboxEvent> findPendingEvents() {
        return outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events = findPendingEvents();

        for (OutboxEvent event : events) {

            Optional<OutboxEvent> claimedEvent =
                   outboxEventService.claimEvent(event.getId());

            if (claimedEvent.isEmpty()) {
                continue;
            }

            OutboxEvent eventToPublish = claimedEvent.get();

            try {

                kafkaTemplate.send(
                        getTopic(eventToPublish),
                        eventToPublish.getAggregateId(),
                        eventToPublish.getPayload()
                ).get();

                outboxEventService.markAsPublished(eventToPublish.getId());

                System.out.println(
                        "[Outbox] Published event "
                                + eventToPublish.getId()
                );

            } catch (Exception exception) {

                outboxEventService.resetToPending(eventToPublish.getId());

                System.out.println(
                        "[Outbox] Failed to publish event "
                                + eventToPublish.getId()
                                + ": "
                                + exception.getMessage()
                );
            }
        }
    }



    private String getTopic(OutboxEvent event) {

        return switch (event.getEventType()) {
            case "InventoryReservedEvent" -> "inventory.reserved";
            case "ProductSavedEvent" -> "inventory.product.saved";
            default -> throw new IllegalStateException("Unexpected value: " + event.getEventType());
        };
    }

    @Scheduled(fixedDelay = 60000)
    public void recoverStuckEvents() {
        outboxEventService.recoverStuckEvents();
    }

}