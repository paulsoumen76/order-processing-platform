package com.orderprocessing.notification.repository;

import com.orderprocessing.notification.model.ProductNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductNotificationRepository
        extends JpaRepository<ProductNotification, Long> {

    List<ProductNotification> findByProductId(String productId);
}