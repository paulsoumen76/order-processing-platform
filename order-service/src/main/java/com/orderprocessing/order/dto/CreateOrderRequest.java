package com.orderprocessing.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(

        @NotBlank(message = "Product ID must not be blank")
        String productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Email(message = "Please provide a valid email address")
        String notificationEmail
) {

}