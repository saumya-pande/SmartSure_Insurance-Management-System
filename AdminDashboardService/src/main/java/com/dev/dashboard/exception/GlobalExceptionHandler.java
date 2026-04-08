package com.dev.dashboard.exception;

import feign.FeignException;
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

    // ── 400 Bad Request — invalid operation ──────────────────────────────────
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(
            InvalidOperationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Invalid Operation", ex.getMessage(), req);
    }

    // ── 503 Service Unavailable — circuit breaker fallback ───────────────────
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(
            ServiceUnavailableException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage(), req);
    }

    // ── Feign errors — decode downstream HTTP errors ─────────────────────────
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, HttpServletRequest req) {

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null || ex.status() <= 0) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            return build(status, "Service Unavailable", "The downstream microservice is offline or unreachable.", req);
        }

        String error = status.getReasonPhrase();
        String message = ex.getMessage();

        try {
            if (ex.contentUTF8() != null && !ex.contentUTF8().isBlank()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(ex.contentUTF8());
                if (node.has("message")) {
                   message = node.get("message").asText();
                }
                if (node.has("error")) {
                   error = node.get("error").asText();
                }
            }
        } catch (Exception ignored) {
            if (status == HttpStatus.NOT_FOUND) {
                message = "The requested resource was not found in the downstream service.";
            } else if (status == HttpStatus.BAD_REQUEST) {
                message = "The downstream service rejected the request.";
            }
        }

        return build(status, error, message, req);
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

    // ── 503 Service Unavailable — Circuit Breaker Open ────────────────────────
    @ExceptionHandler(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreakerOpen(
            io.github.resilience4j.circuitbreaker.CallNotPermittedException ex, HttpServletRequest req) {

        return build(HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            "The downstream service is currently overloaded or unresponsive. Circuit breaker is open. Please try again later.",
            req);
    }

    // ── 500 Circuit Breaker Wrapper Unwrapping ────────────────────────────────
    @ExceptionHandler(org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNoFallbackAvailable(
            org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException ex, HttpServletRequest req) {
        
        Throwable cause = ex.getCause();
        
        // Unwrap FeignExceptions (e.g., 404, 400 from downstream)
        if (cause instanceof FeignException) {
            return handleFeignException((FeignException) cause, req);
        }
        
        // Unwrap Circuit Breaker Open state
        if (cause instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            return handleCircuitBreakerOpen((io.github.resilience4j.circuitbreaker.CallNotPermittedException) cause, req);
        }
        
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred down stream without a fallback: " + (cause != null ? cause.getMessage() : ex.getMessage()),
            req);
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
