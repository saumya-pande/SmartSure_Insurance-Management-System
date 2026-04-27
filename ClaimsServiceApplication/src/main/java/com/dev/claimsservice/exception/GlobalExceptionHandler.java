package com.dev.claimsservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ────────────────────────────────────────────────────────
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            EntityNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    // ── 409 Conflict — duplicate resource ─────────────────────────────────────
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Duplicate Resource", ex.getMessage(), req);
    }

    // ── 400 Bad Request — invalid operation ──────────────────────────────────
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(
            InvalidOperationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Invalid Operation", ex.getMessage(), req);
    }

    // ── Downstream Service Errors ─────────────────────────────────────────
    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<ErrorResponse> handleDownstreamServiceException(
            DownstreamServiceException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getError(), ex.getMessage(), req);
    }

    // ── 400 Bad Request — @Valid annotation failures ─────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        java.util.List<ErrorResponse.ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ErrorResponse.ValidationError(err.getField(), err.getDefaultMessage()))
                .toList();

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid input data")
                .path(req.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── 400 Bad Request — wrong type for @RequestParam / @PathVariable ───────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {

        Class<?> requiredType = ex.getRequiredType();
        String expected = (requiredType != null) ? requiredType.getSimpleName() : "unknown";

        String message = String.format(
            "Parameter '%s' has invalid value '%s'. Expected type: %s.",
            ex.getName(), ex.getValue(), expected
        );
        return build(HttpStatus.BAD_REQUEST, "Type Mismatch", message, req);
    }

    // ── 400 Bad Request — missing @RequestParam entirely ─────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {

        String message = String.format(
            "Required request parameter '%s' of type '%s' is missing.",
            ex.getParameterName(), ex.getParameterType()
        );
        return build(HttpStatus.BAD_REQUEST, "Missing Parameter", message, req);
    }

    // ── 400 Bad Request — unreadable / malformed JSON body ───────────────────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        String message = "Request body is missing or malformed. " +
                         "Check JSON syntax and field types.";
        return build(HttpStatus.BAD_REQUEST, "Malformed Request Body", message, req);
    }

    // ── 403 Forbidden — @PreAuthorize failures ───────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {

        return build(HttpStatus.FORBIDDEN,
            "Access Denied",
            "You do not have permission to access this resource. " +
            "Required role is missing or insufficient.",
            req);
    }

    // ── 401 Unauthorized — no/invalid auth headers ───────────────────────────
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            AuthenticationException ex, HttpServletRequest req) {

        return build(HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "Authentication is required. Please provide a valid JWT token.",
            req);
    }

    // ── 400 Bad Request — JPA Constraint Violations (e.g., persist time) ──────
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex, HttpServletRequest req) {

        java.util.List<ErrorResponse.ValidationError> errors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    return new ErrorResponse.ValidationError(path, violation.getMessage());
                })
                .toList();

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid input data at persistence layer")
                .path(req.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── 500 fallback ─────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {

        return build(HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred: " + ex.getMessage(),
            req);
    }

    // ── builder helper ────────────────────────────────────────────────────────
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message, HttpServletRequest req) {

        return ResponseEntity.status(status).body(
            ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
