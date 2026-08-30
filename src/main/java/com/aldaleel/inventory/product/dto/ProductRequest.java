package com.aldaleel.inventory.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a product.
 * TASK-004: Validation annotations ensure data integrity at the API boundary.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be zero or positive")
    private BigDecimal price;

    // TASK-003 / TASK-009: initial stock — cannot be negative
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be zero or positive")
    private Integer stockQuantity;
}
