package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.dto.ProductUnitResponse;
import com.orderprocessing.inventory.model.TrackingType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SerializedReservationStrategy
        implements InventoryReservationStrategy {

    private final ProductUnitService productUnitService;

    public SerializedReservationStrategy(
            ProductUnitService productUnitService) {
        this.productUnitService = productUnitService;
    }

    @Override
    public boolean supports(TrackingType trackingType) {
        return trackingType == TrackingType.SERIALIZED;
    }

    @Override
    public boolean reserve(
            String productId,
            int quantity) {

        List<ProductUnitResponse> reservedUnits =
                productUnitService.reserveUnits(
                        productId,
                        quantity
                );

        return !reservedUnits.isEmpty();
    }
}