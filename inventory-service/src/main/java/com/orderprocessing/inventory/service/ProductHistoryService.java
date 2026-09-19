package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.dto.ProductHistoryResponse;
import com.orderprocessing.inventory.model.ProductHistory;
import com.orderprocessing.inventory.model.ProductHistoryAction;
import com.orderprocessing.inventory.repository.ProductHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductHistoryService {

    private final ProductHistoryRepository productHistoryRepository;

    public ProductHistoryService(
            ProductHistoryRepository productHistoryRepository) {

        this.productHistoryRepository = productHistoryRepository;
    }

    public void record(
            String productId,
            ProductHistoryAction action,
            String fieldName,
            String oldValue,
            String newValue) {

        ProductHistory history = new ProductHistory();

        history.setProductId(productId);
        history.setAction(action);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);

        // Authentication will be added later.
        history.setChangedBy("SYSTEM");

        history.setChangedAt(LocalDateTime.now());

        productHistoryRepository.save(history);
    }

    public List<ProductHistoryResponse> getHistory(
            String productId) {

        return productHistoryRepository
                .findByProductIdOrderByChangedAtDesc(productId)
                .stream()
                .map(history ->
                        new ProductHistoryResponse(
                                history.getAction(),
                                history.getFieldName(),
                                history.getOldValue(),
                                history.getNewValue(),
                                history.getChangedBy(),
                                history.getChangedAt()
                        )
                )
                .toList();
    }
}