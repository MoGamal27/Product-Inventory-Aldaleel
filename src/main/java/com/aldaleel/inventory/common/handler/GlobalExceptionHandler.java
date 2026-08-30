package com.aldaleel.inventory.common.handler;

import com.aldaleel.inventory.common.exception.DuplicateResourceException;
import com.aldaleel.inventory.common.exception.InsufficientStockException;
import com.aldaleel.inventory.common.exception.InvalidOrderStateException;
import com.aldaleel.inventory.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralised exception → HTTP response mapping for the entire API.
 *
 * Every response follows the same ErrorResponse envelope so clients
 * have a consistent shape to parse regardless of the error type.
 *
 * Handled exceptions:
 *   ResourceNotFoundException      → 404 Not Found
 *   DuplicateResourceException     → 409 Conflict
 *   InsufficientStockException     → 409 Conflict
 *   InvalidOrderStateException     → 422 Unprocessable Entity
 *   MethodArgumentNotValidException→ 400 Bad Request  (field-level detail)
 *   OptimisticLockingFailureException→ 409 Conflict   (TASK-010 fallback)
 *   Exception (catch-all)          → 500 Internal Server Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----------------------------------------------------------------
    // 404 — Resource not found
    // ----------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    // ----------------------------------------------------------------
    // 409 — Conflict (duplicate resource)
    // ----------------------------------------------------------------

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    // ----------------------------------------------------------------
    // 409 — Conflict (insufficient stock — TASK-007 / TASK-009)
    // ----------------------------------------------------------------

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    // ----------------------------------------------------------------
    // 409 — Conflict (optimistic lock collision — TASK-010 fallback)
    // When two transactions race and one loses the @Version check,
    // Spring throws OptimisticLockingFailureException. We surface this
    // as a 409 so clients know to retry.
    // ----------------------------------------------------------------

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            OptimisticLockingFailureException ex, HttpServletRequest request) {

        return build(
                HttpStatus.CONFLICT,
                "The resource was modified by another request. Please retry.",
                request.getRequestURI());
    }

    // ----------------------------------------------------------------
    // 422 — Unprocessable Entity (invalid order state — TASK-015)
    // ----------------------------------------------------------------

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderState(
            InvalidOrderStateException ex, HttpServletRequest request) {

        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
    }

    // ----------------------------------------------------------------
    // 400 — Validation errors (TASK-004)
    // Returns a field → message map inside the errors key so clients
    // can highlight individual form fields.
    // ----------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            // keep only the first message per field
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed. Check the 'errors' field for details.")
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ----------------------------------------------------------------
    // 500 — Catch-all for unexpected errors
    // ----------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        // Log at error level in production; omitted here to keep the
        // handler dependency-free (no logger injection needed).
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.",
                request.getRequestURI());
    }

    // ----------------------------------------------------------------
    // Builder helpers
    // ----------------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, String path) {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
