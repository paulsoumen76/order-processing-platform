package com.orderprocessing.notification.repository;

import com.orderprocessing.notification.model.OutboxEvent;
import com.orderprocessing.notification.model.OutboxEventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(
            OutboxEventStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT o
    FROM OutboxEvent o
    WHERE o.id = :id
      AND o.status = :status
""")
    Optional<OutboxEvent> findByIdAndStatusForUpdate(
            @Param("id") Long id,
            @Param("status") OutboxEventStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT o
    FROM OutboxEvent o
    WHERE o.status = :status
      AND o.leaseUntil < :time
""")
    List<OutboxEvent> findExpiredProcessingEventsForUpdate(
            @Param("status") OutboxEventStatus status,
            @Param("time") LocalDateTime time);
}