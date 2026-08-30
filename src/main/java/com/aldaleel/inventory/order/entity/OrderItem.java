package com.aldaleel.inventory.order.entity;

import com.aldaleel.inventory.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * TASK-006: OrderItem entity — one line in an order.
 * Links an Order to a Product with the reserved quantity.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many items belong to one order.
     * FetchType.LAZY avoids unnecessary joins when loading items individually.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Many items can reference the same product.
     * FetchType.LAZY — product is loaded only when accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * TASK-007 / TASK-009: Quantity reserved — must be at least 1.
     * Stored to allow stock restoration on cancellation (TASK-013).
     */
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Snapshot of the unit price at the time of the order.
     * Prevents price changes from affecting existing orders.
     */
    @Column(nullable = false)
    private java.math.BigDecimal unitPrice;
}
