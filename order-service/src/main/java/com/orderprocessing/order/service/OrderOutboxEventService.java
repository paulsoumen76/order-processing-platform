package com.orderprocessing.order.service;

import com.orderprocessing.order.model.OutboxEvent;
import com.orderprocessing.order.model.OutboxEventStatus;
import com.orderprocessing.order.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderOutboxEventService {

    private final String workerId = UUID.randomUUID().toString();

    private final OutboxEventRepository outboxEventRepository;

    public OrderOutboxEventService(
            OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public Optional<OutboxEvent> claimEvent(Long eventId) {

        Optional<OutboxEvent> optionalEvent =
                outboxEventRepository.findByIdAndStatusForUpdate(
                        eventId,
                        OutboxEventStatus.PENDING
                );

        if (optionalEvent.isEmpty()) {
            return Optional.empty();
        }

        OutboxEvent event = optionalEvent.get();

        event.setStatus(OutboxEventStatus.PROCESSING);
        event.setWorkerId(workerId);
        event.setLeaseUntil(
                LocalDateTime.now().plusMinutes(2)
        );

        outboxEventRepository.save(event);

        return Optional.of(event);
    }

    @Transactional
    public void markAsPublished(Long eventId) {

        OutboxEvent event =
                outboxEventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Outbox event not found: "
                                                + eventId));

        event.setStatus(OutboxEventStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        event.setLeaseUntil(null);
        event.setWorkerId(null);

        outboxEventRepository.save(event);
    }

    @Transactional
    public void resetToPending(Long eventId) {

        OutboxEvent event =
                outboxEventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Outbox event not found: "
                                                + eventId));

        event.setStatus(OutboxEventStatus.PENDING);
        event.setLeaseUntil(null);
        event.setWorkerId(null);

        outboxEventRepository.save(event);
    }

    @Transactional
    public void recoverStuckEvents() {

        LocalDateTime now = LocalDateTime.now();

        List<OutboxEvent> stuckEvents =
                outboxEventRepository
                        .findExpiredProcessingEventsForUpdate(
                                OutboxEventStatus.PROCESSING,
                                now
                        );

        for (OutboxEvent event : stuckEvents) {
            event.setStatus(OutboxEventStatus.PENDING);
            event.setLeaseUntil(null);
            event.setWorkerId(null);
        }

        outboxEventRepository.saveAll(stuckEvents);
    }
}