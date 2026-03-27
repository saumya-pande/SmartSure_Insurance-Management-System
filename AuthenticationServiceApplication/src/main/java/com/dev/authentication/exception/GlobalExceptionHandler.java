package com.dev.authentication.exception;

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
import java.util.LinkedHashMap;
import java.util.Map;

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

    // ── 400 Bad Request — @Valid annotation failures ─────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields failed validation. See 'fieldErrors' for details.")
                .path(req.getRequestURI())
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
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
                ex.getName(), ex.getValue(), expected);
        return build(HttpStatus.BAD_REQUEST, "Type Mismatch", message, req);
    }

    // ── 400 Bad Request — missing @RequestParam entirely ─────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {

        String message = String.format(
                "Required request parameter '%s' of type '%s' is missing.",
                ex.getParameterName(), ex.getParameterType());
        return build(HttpStatus.BAD_REQUEST, "Missing Parameter", message, req);
    }

    // ── 400 Bad Request — unreadable / malformed JSON body ───────────────────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        String message;
        Map<String, String> fieldErrors = null;

        Throwable cause = ex.getCause();
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {
            // e.g. invalid enum value for "role"
            String fieldName = ife.getPath().isEmpty() ? "unknown"
                    : ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            String targetType = ife.getTargetType() != null
                    ? ife.getTargetType().getSimpleName()
                    : "unknown";
            message = String.format("Invalid value '%s' for field '%s'. Expected type: %s.",
                    ife.getValue(), fieldName, targetType);
            fieldErrors = new LinkedHashMap<>();
            fieldErrors.put(fieldName, message);

        } else if (cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException mie) {
            // e.g. missing required field, wrong JSON structure
            String fieldName = mie.getPath().isEmpty() ? "unknown"
                    : mie.getPath().get(mie.getPath().size() - 1).getFieldName();
            String targetType = mie.getTargetType() != null
                    ? mie.getTargetType().getSimpleName()
                    : "unknown";
            message = String.format("Field '%s' has an invalid value or type. Expected: %s.",
                    fieldName, targetType);
            fieldErrors = new LinkedHashMap<>();
            fieldErrors.put(fieldName, message);

        } else if (cause instanceof com.fasterxml.jackson.core.JsonParseException) {
            message = "Request body contains invalid JSON syntax. " +
                    "Check for missing commas, quotes, or brackets.";
        } else {
            message = "Request body is missing or malformed. " +
                    "Check JSON syntax and field types.";
        }

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Malformed Request Body")
                .message(message)
                .path(req.getRequestURI())
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
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
                        .build());
    }
}
