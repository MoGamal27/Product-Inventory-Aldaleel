package com.aldaleel.inventory.common.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Extended error envelope for @Valid / @Validated failures (HTTP 400).
 * Adds an "errors" map so clients can show per-field messages.
 *
 * Example JSON:
 * {
 *   "timestamp": "2026-08-30T10:15:30",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed. Check the 'errors' field for details.",
 *   "path": "/api/v1/products",
 *   "errors": {
 *     "name": "Product name is required",
 *     "stockQuantity": "Stock quantity must be zero or positive"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /** Field name → validation message pairs. */
    private Map<String, String> errors;
}
