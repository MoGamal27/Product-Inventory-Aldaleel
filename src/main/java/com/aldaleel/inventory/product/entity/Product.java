package com.aldaleel.inventory.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TASK-001: Product entity representing a product in the inventory.
 * TASK-003: Includes stockQuantity field for inventory management.
 * TASK-010: Uses @Version for optimistic locking to handle concurrent updates.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TASK-004: validated — must not be blank
    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // TASK-004: price must be >= 0
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be zero or positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // TASK-003: stock quantity — must be >= 0 (never negative)
    // TASK-009: @Min(0) enforces non-negative stock at entity level
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be zero or positive")
    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // TASK-010: optimistic locking version field to handle concurrent requests
    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
