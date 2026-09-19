package com.orderprocessing.notification.idempotency;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_processed_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_processed_event",
                        columnNames = {"event_id", "consumer"}
                )
        }
)
public class ProcessedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String consumer;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public ProcessedEvent(
            String eventId,
            String consumer,
            LocalDateTime processedAt
    ) {
        this.eventId = eventId;
        this.consumer = consumer;
        this.processedAt = processedAt;
    }

}