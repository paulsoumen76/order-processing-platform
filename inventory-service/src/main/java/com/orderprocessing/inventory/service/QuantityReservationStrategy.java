package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.model.TrackingType;
import com.orderprocessing.inventory.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class QuantityReservationStrategy
        implements InventoryReservationStrategy {

    private final ProductRepository productRepository;

    public QuantityReservationStrategy(
            ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean supports(TrackingType trackingType) {
        return trackingType == TrackingType.QUANTITY;
    }

    @Override
    public boolean reserve(
            String productId,
            int quantity) {

        int updatedRows =
                productRepository.reserveQuantity(
                        productId,
                        quantity
                );

        return updatedRows != 0;
    }
}