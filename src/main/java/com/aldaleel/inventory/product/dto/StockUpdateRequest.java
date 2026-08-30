package com.aldaleel.inventory.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin stock quantity updates (TASK-003).
 * Separated from ProductRequest so admins can adjust stock independently.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateRequest {

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be zero or positive")
    private Integer stockQuantity;
}
