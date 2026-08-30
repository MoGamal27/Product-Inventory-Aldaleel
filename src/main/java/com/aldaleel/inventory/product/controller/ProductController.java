package com.aldaleel.inventory.product.controller;

import com.aldaleel.inventory.product.dto.ProductRequest;
import com.aldaleel.inventory.product.dto.ProductResponse;
import com.aldaleel.inventory.product.dto.StockUpdateRequest;
import com.aldaleel.inventory.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * TASK-002: REST endpoints for product management (admin use).
 * TASK-005: Supports pagination via Pageable.
 *
 * Base URL: /api/v1/products
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * TASK-005: Get all products with pagination.
     * GET /api/v1/products?page=0&size=10&sort=name,asc
     */
    @GetMapping
    public Page<ProductResponse> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return productService.findAll(pageable);
    }

    /**
     * TASK-002: Get a single product by ID.
     * GET /api/v1/products/{id}
     */
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    /**
     * TASK-002 + TASK-004: Create a new product with validation.
     * POST /api/v1/products
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    /**
     * TASK-002: Update an existing product.
     * PUT /api/v1/products/{id}
     */
    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    /**
     * TASK-003: Update only the stock quantity of a product.
     * PATCH /api/v1/products/{id}/stock
     */
    @PatchMapping("/{id}/stock")
    public ProductResponse updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return productService.updateStock(id, request);
    }

    /**
     * TASK-002: Delete a product.
     * DELETE /api/v1/products/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
