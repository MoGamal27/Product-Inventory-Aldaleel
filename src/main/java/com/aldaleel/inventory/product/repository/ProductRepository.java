package com.aldaleel.inventory.product.repository;

import com.aldaleel.inventory.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TASK-002: Repository for Product CRUD operations.
 * TASK-010: Provides a pessimistic-write lock query to handle concurrent stock updates.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * TASK-010: Acquires a PESSIMISTIC_WRITE lock on the product row.
     * Prevents race conditions when multiple orders reserve stock simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    boolean existsByName(String name);
}
