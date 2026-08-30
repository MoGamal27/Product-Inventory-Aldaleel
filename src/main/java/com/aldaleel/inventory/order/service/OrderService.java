package com.aldaleel.inventory.order.service;

import com.aldaleel.inventory.common.exception.InsufficientStockException;
import com.aldaleel.inventory.common.exception.InvalidOrderStateException;
import com.aldaleel.inventory.common.exception.ResourceNotFoundException;
import com.aldaleel.inventory.order.dto.OrderItemRequest;
import com.aldaleel.inventory.order.dto.OrderItemResponse;
import com.aldaleel.inventory.order.dto.OrderRequest;
import com.aldaleel.inventory.order.dto.OrderResponse;
import com.aldaleel.inventory.order.entity.Order;
import com.aldaleel.inventory.order.entity.OrderItem;
import com.aldaleel.inventory.order.entity.OrderStatus;
import com.aldaleel.inventory.order.repository.OrderRepository;
import com.aldaleel.inventory.product.entity.Product;
import com.aldaleel.inventory.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Core order service covering US-002 and US-003.
 *
 * TASK-005  – create order
 * TASK-006  – create order items
 * TASK-007  – validate stock availability before reserving
 * TASK-008  – reserve (deduct) stock atomically
 * TASK-009  – prevent negative stock
 * TASK-010  – handle concurrent requests via PESSIMISTIC_WRITE lock
 * TASK-011  – @Transactional ensures full rollback on any failure
 * TASK-012  – implement order cancellation
 * TASK-013  – restore stock on cancellation
 * TASK-014  – prevent double release (idempotent cancel check)
 * TASK-015  – enforce valid state transitions
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // ----------------------------------------------------------------
    // TASK-005 / TASK-006 / TASK-007 / TASK-008 / TASK-009
    // TASK-010 / TASK-011 — Create order & reserve stock
    // ----------------------------------------------------------------

    /**
     * Creates an order and reserves stock for every requested item.
     *
     * Concurrency strategy (TASK-010):
     *   Each product row is locked with PESSIMISTIC_WRITE before reading
     *   its stock. This serialises concurrent orders for the same product,
     *   guaranteeing stock never goes negative (TASK-009).
     *
     * Rollback strategy (TASK-011):
     *   The whole method runs in a single transaction. If ANY product has
     *   insufficient stock the exception unwinds the transaction and all
     *   previously deducted quantities are automatically restored.
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {

            // TASK-010: acquire a row-level write lock — blocks concurrent
            // transactions that try to modify the same product row
            Product product = productRepository
                    .findByIdWithLock(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemReq.getProductId()));

            // TASK-007: verify enough stock exists before touching anything
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                // TASK-011: throwing here triggers a full transaction rollback
                throw new InsufficientStockException(
                        product.getName(),
                        itemReq.getQuantity(),
                        product.getStockQuantity());
            }

            // TASK-008 + TASK-009: deduct stock — never goes negative because
            // the check above and the lock together make this atomic
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            // TASK-006: build the order item with a price snapshot
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())   // price captured at order time
                    .build();

            items.add(item);
        }

        order.setItems(items);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // TASK-012 / TASK-013 / TASK-014 / TASK-015 — Cancel order
    // ----------------------------------------------------------------

    /**
     * Cancels an order and restores all reserved stock.
     *
     * TASK-014: If the order is already CANCELLED the method throws —
     *           preventing a double-release of stock.
     * TASK-015: DELIVERED orders cannot be cancelled — invalid transition.
     * TASK-013: Stock is restored inside the same transaction so a failure
     *           mid-loop won't leave inventory in a partial state.
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        // TASK-015 + TASK-014: validate the transition is allowed
        validateCancellationTransition(order);

        // TASK-013: restore each item's reserved quantity
        for (OrderItem item : order.getItems()) {

            // TASK-010: lock the product row before restoring stock
            Product product = productRepository
                    .findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + item.getProduct().getId()));

            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        // TASK-012: mark as cancelled
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    // ----------------------------------------------------------------
    // Read operations
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAllWithItems(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + id));
        return toResponse(order);
    }

    // ----------------------------------------------------------------
    // TASK-015 — State transition guard
    // ----------------------------------------------------------------

    /**
     * Enforces valid cancellation transitions.
     *
     * | Current status | Allowed? | Reason                              |
     * |----------------|----------|-------------------------------------|
     * | PENDING        | YES      | Normal cancellation path            |
     * | CONFIRMED      | YES      | Allowed before delivery             |
     * | CANCELLED      | NO       | Already cancelled — double release  |
     * | DELIVERED      | NO       | Terminal state — cannot undo        |
     */
    private void validateCancellationTransition(Order order) {
        switch (order.getStatus()) {
            case CANCELLED ->
                    // TASK-014: prevent double release
                    throw new InvalidOrderStateException(
                            "Order " + order.getId() + " is already cancelled.");
            case DELIVERED ->
                    // TASK-015: terminal state — no going back
                    throw new InvalidOrderStateException(
                            "Order " + order.getId() + " has been delivered and cannot be cancelled.");
            default -> { /* PENDING and CONFIRMED are cancellable */ }
        }
    }

    // ----------------------------------------------------------------
    // Mapping helpers
    // ----------------------------------------------------------------

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(OrderItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .items(itemResponses)
                .totalAmount(total)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(subtotal)
                .build();
    }
}
