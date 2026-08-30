package com.aldaleel.inventory.order.entity;

/**
 * TASK-015: Defines all valid order states for the state machine.
 *
 * Allowed transitions:
 *   PENDING  → CONFIRMED
 *   PENDING  → CANCELLED
 *   CONFIRMED → CANCELLED  (only if not yet delivered)
 *   CONFIRMED → DELIVERED
 *   CANCELLED → (terminal — no further transitions)
 *   DELIVERED → (terminal — no further transitions)
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    DELIVERED
}
