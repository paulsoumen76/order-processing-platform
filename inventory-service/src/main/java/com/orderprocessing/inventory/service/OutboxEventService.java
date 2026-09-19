package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.model.OutboxEvent;
import com.orderprocessing.inventory.model.OutboxEventStatus;
import com.orderprocessing.inventory.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OutboxEventService {

    private final String workerId = UUID.randomUUID().toString();
    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public Optional<OutboxEvent> claimEvent(Long eventId) {
        Optional<OutboxEvent> optionalEvent =
                outboxEventRepository
                        .findByIdAndStatusForUpdate(
                                eventId,
                                OutboxEventStatus.PENDING
                        );

        if (optionalEvent.isEmpty()) {
            return Optional.empty();
        }

        OutboxEvent event = optionalEvent.get();

        event.setStatus(
                OutboxEventStatus.PROCESSING
        );
        event.setLeaseUntil(LocalDateTime.now().plusMinutes(2));
        event.setWorkerId(workerId);
        outboxEventRepository.save(event);

        return Optional.of(event);
    }

    @Transactional
    public void markAsPublished(Long eventId) {

        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Outbox event not found: " + eventId));

        event.setStatus(OutboxEventStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        event.setLeaseUntil(null);
        event.setWorkerId(null);

        outboxEventRepository.save(event);
    }


    @Transactional
    public void resetToPending(Long eventId) {

        OutboxEvent event =
                outboxEventRepository
                        .findById(eventId)
                        .orElseThrow();

        event.setStatus(
                OutboxEventStatus.PENDING
        );
        event.setLeaseUntil(null);
        event.setWorkerId(null);

        outboxEventRepository.save(event);
    }

    @Transactional
    public void recoverStuckEvents() {

        LocalDateTime now = LocalDateTime.now();

        List<OutboxEvent> stuckEvents =
                outboxEventRepository.findExpiredProcessingEventsForUpdate(
                        OutboxEventStatus.PROCESSING,
                        now);

        for (OutboxEvent event : stuckEvents) {
            event.setStatus(OutboxEventStatus.PENDING);
            event.setLeaseUntil(null);
            event.setWorkerId(null);
        }

        outboxEventRepository.saveAll(stuckEvents);
    }

}