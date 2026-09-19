package com.orderprocessing.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReceiveProductUnitsRequest(
        @NotEmpty(message = "At least one product unit is required")
        @Valid
        List<ProductUnitRequest> units
) {
}
