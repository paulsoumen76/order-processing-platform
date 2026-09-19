package com.orderprocessing.notification.service;

import com.orderprocessing.notification.model.OutboxEvent;
import com.orderprocessing.notification.model.OutboxEventStatus;
import com.orderprocessing.notification.repository.OutboxEventRepository;
import com.orderprocessing.notification.strategy.NotificationStrategy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationOutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationStrategyFactory strategyFactory;
    private final NotificationOutboxEventService outboxEventService;

    public NotificationOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            NotificationStrategyFactory strategyFactory,
            NotificationOutboxEventService outboxEventService) {
        this.outboxEventRepository = outboxEventRepository;
        this.strategyFactory = strategyFactory;
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

            OutboxEvent eventToProcess = claimedEvent.get();

            try {
                processEvent(eventToProcess);
                outboxEventService.markAsPublished(eventToProcess.getId());
            } catch (Exception exception) {
                outboxEventService.resetToPending(eventToProcess.getId());
                System.out.println(
                        "[Notification Outbox] Failed event "
                                + eventToProcess.getId()
                                + ": " + exception.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 4000)
    public void recoverStuckEvents() {
        outboxEventService.recoverStuckEvents();
    }

    private void processEvent(OutboxEvent event) {
        NotificationStrategy strategy =
                strategyFactory.getStrategy(
                        event.getEventType(),
                        event.getNotificationType());
        strategy.send(event);
    }
}
