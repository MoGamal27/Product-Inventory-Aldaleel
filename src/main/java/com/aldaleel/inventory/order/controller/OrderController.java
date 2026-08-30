package com.aldaleel.inventory.order.controller;

import com.aldaleel.inventory.order.dto.OrderRequest;
import com.aldaleel.inventory.order.dto.OrderResponse;
import com.aldaleel.inventory.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for order management (US-002 and US-003).
 *
 * Base URL: /api/v1/orders
 *
 * POST   /api/v1/orders              – create order & reserve stock (TASK-005 to TASK-011)
 * GET    /api/v1/orders              – list all orders (paginated)
 * GET    /api/v1/orders/{id}         – get single order
 * PATCH  /api/v1/orders/{id}/cancel  – cancel order & restore stock (TASK-012 to TASK-015)
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * TASK-005/006/007/008/009/010/011:
     * Create a new order. Validates stock for each item, reserves it,
     * and rolls back the whole transaction if any item fails.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    /**
     * List all orders with pagination.
     * GET /api/v1/orders?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public Page<OrderResponse> getAll(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return orderService.findAll(pageable);
    }

    /**
     * Get a single order by ID.
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    /**
     * TASK-012/013/014/015:
     * Cancel an order. Restores stock for all items.
     * Returns 422 if the order is already cancelled or delivered.
     * PATCH /api/v1/orders/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}
