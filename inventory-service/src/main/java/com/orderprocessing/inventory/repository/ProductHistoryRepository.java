package com.orderprocessing.inventory.repository;

import com.orderprocessing.inventory.model.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductHistoryRepository
        extends JpaRepository<ProductHistory, Long> {

    List<ProductHistory> findByProductIdOrderByChangedAtDesc(
            String productId
    );
}