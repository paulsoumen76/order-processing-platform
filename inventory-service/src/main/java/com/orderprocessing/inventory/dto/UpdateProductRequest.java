package com.orderprocessing.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @NotBlank
        String productName,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal price,

        @Email
        String notificationEmail,

        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Invalid mobile number"
        )
        String notificationMobile
) {
}