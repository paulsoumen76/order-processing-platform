package com.orderprocessing.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductUnitRequest(
        @NotBlank(message = "Serial number must not be blank")
        String serialNumber
) {
}
