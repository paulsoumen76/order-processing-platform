package com.orderprocessing.inventory.dto;

import jakarta.validation.constraints.Min;

public record ReserveQuantityRequest(

        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity

) {}