package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.model.TrackingType;

public interface InventoryReservationStrategy {

    boolean supports(TrackingType trackingType);

    boolean reserve(String productId, int quantity);
}