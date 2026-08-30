package com.aldaleel.inventory.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for returning a single order line item to the client.
 * Includes a product snapshot (id + name) so the client never needs
 * to make a second request to resolve the product.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;

    /** Unit price captured at order time — immune to later price changes. */
    private BigDecimal unitPrice;

    /** Convenience field: unitPrice × quantity */
    private BigDecimal subtotal;
}
