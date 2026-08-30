package com.aldaleel.inventory.product.service;

import com.aldaleel.inventory.common.exception.DuplicateResourceException;
import com.aldaleel.inventory.common.exception.ResourceNotFoundException;
import com.aldaleel.inventory.product.dto.ProductRequest;
import com.aldaleel.inventory.product.dto.ProductResponse;
import com.aldaleel.inventory.product.dto.StockUpdateRequest;
import com.aldaleel.inventory.product.entity.Product;
import com.aldaleel.inventory.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TASK-002: Implements CRUD operations for products.
 * TASK-003: Manages stock quantity updates.
 * TASK-004: Business-level validation (duplicate name check).
 * TASK-005: Supports paginated product listing.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ----------------------------------------------------------------
    // TASK-005: Paginated product listing
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ----------------------------------------------------------------
    // TASK-002: Get single product
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = getProductOrThrow(id);
        return toResponse(product);
    }

    // ----------------------------------------------------------------
    // TASK-002: Create product
    // TASK-004: Validate no duplicate name
    // ----------------------------------------------------------------

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Product with name '" + request.getName() + "' already exists");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();

        return toResponse(productRepository.save(product));
    }

    // ----------------------------------------------------------------
    // TASK-002: Update product
    // TASK-004: Validate no duplicate name on update
    // ----------------------------------------------------------------

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProductOrThrow(id);

        boolean nameChangedAndTaken = !product.getName().equals(request.getName())
                && productRepository.existsByName(request.getName());

        if (nameChangedAndTaken) {
            throw new DuplicateResourceException(
                    "Product with name '" + request.getName() + "' already exists");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        return toResponse(productRepository.save(product));
    }

    // ----------------------------------------------------------------
    // TASK-003: Standalone stock quantity update
    // ----------------------------------------------------------------

    @Transactional
    public ProductResponse updateStock(Long id, StockUpdateRequest request) {
        Product product = getProductOrThrow(id);
        product.setStockQuantity(request.getStockQuantity());
        return toResponse(productRepository.save(product));
    }

    // ----------------------------------------------------------------
    // TASK-002: Delete product
    // ----------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        Product product = getProductOrThrow(id);
        productRepository.delete(product);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /**
     * Shared lookup used across service methods and by OrderService.
     */
    public Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
    }

    /**
     * Maps a Product entity to a ProductResponse DTO.
     */
    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
