package com.orderprocessing.inventory.idempotency;

import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyService(
            ProcessedEventRepository processedEventRepository
    ) {
        this.processedEventRepository = processedEventRepository;
    }

    public boolean tryProcess(
            String eventId,
            String consumer
    ) {
        return processedEventRepository.tryMarkAsProcessed(
                eventId,
                consumer
        ) == 1;
    }
}