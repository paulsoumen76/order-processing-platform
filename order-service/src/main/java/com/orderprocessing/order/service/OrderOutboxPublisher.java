package com.orderprocessing.order.service;

import com.orderprocessing.order.model.OutboxEvent;
import com.orderprocessing.order.model.OutboxEventStatus;
import com.orderprocessing.order.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderOutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderOutboxEventService outboxEventService;

    public OrderOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            OrderOutboxEventService outboxEventService) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventService = outboxEventService;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> events =
                outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                        OutboxEventStatus.PENDING);

        for (OutboxEvent event : events) {
            Optional<OutboxEvent> claimedEvent =
                    outboxEventService.claimEvent(event.getId());

            if (claimedEvent.isEmpty()) {
                continue;
            }

            OutboxEvent eventToPublish = claimedEvent.get();

            try {
                kafkaTemplate.send(
                        "order.created",
                        eventToPublish.getAggregateId(),
                        eventToPublish.getPayload()).get();

                outboxEventService.markAsPublished(
                        eventToPublish.getId());

            } catch (Exception exception) {
                outboxEventService.resetToPending(
                        eventToPublish.getId());

                System.out.println(
                        "[Order Outbox] Failed event "
                                + eventToPublish.getId()
                                + ": " + exception.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void recoverStuckEvents() {
        outboxEventService.recoverStuckEvents();
    }
}
