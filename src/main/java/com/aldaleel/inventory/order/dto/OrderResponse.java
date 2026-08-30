package com.aldaleel.inventory.order.dto;

import com.aldaleel.inventory.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for returning full order details to the client.
 * Decouples the API response from the internal Order entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;

    /** TASK-015: Current status exposed so clients can track state transitions. */
    private OrderStatus status;

    private List<OrderItemResponse> items;

    /** Total order value — sum of all item subtotals. */
    private BigDecimal totalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
