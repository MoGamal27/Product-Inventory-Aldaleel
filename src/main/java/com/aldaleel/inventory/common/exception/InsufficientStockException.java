package com.aldaleel.inventory.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * TASK-007 / TASK-009: Thrown when a product does not have enough stock
 * to fulfil the requested quantity.
 * Maps to HTTP 409 Conflict — the request is valid but cannot be processed
 * given the current inventory state.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format(
                "Insufficient stock for product '%s': requested %d, available %d",
                productName, requested, available));
    }
}
