package com.aldaleel.inventory.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for returning product data to the client.
 * Decouples the API contract from the internal entity model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;

    // TASK-003: stock quantity is always visible in the response
    private Integer stockQuantity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
