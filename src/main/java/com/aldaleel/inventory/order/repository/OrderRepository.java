package com.aldaleel.inventory.order.repository;

import com.aldaleel.inventory.order.entity.Order;
import com.aldaleel.inventory.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * TASK-005: Repository for Order persistence.
 * Provides paginated listing and eager-fetch of items to avoid N+1 queries.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Fetch orders with their items in a single query (avoids N+1).
     * Used in listing and detail endpoints.
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product")
    Page<Order> findAllWithItems(Pageable pageable);

    /**
     * Fetch a single order with all items and products eagerly loaded.
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    java.util.Optional<Order> findByIdWithItems(@Param("id") Long id);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
