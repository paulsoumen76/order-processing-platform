package com.orderprocessing.notification.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, Long> {

    @Modifying
    @Query(
            value = """
                    INSERT INTO notification_processed_events
                        (event_id, consumer, processed_at)
                    VALUES
                        (:eventId, :consumer, CURRENT_TIMESTAMP)
                    ON CONFLICT (event_id, consumer) DO NOTHING
                    """,
            nativeQuery = true
    )
    int tryMarkAsProcessed(
            @Param("eventId") String eventId,
            @Param("consumer") String consumer
    );
}