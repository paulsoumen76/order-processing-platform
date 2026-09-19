package com.orderprocessing.inventory.dto;

import java.util.List;

public record InventoryReservationResult(
        boolean reserved,
        List<ProductUnitResponse> reservedUnits
) {
}