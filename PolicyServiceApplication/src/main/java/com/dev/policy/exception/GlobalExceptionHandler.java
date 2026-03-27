package com.dev.policy.exception;


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
    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            PolicyNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    // ── 409 Conflict — duplicate property/vehicle ─────────────────────────
    @ExceptionHandler(DuplicatePropertyInsuranceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicatePropertyInsuranceException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Duplicate Insurance", ex.getMessage(), req);
    }

    // ── 422 Unprocessable — premium out of range ──────────────────────────
    @ExceptionHandler(PremiumOutOfRangeException.class)
    public ResponseEntity<ErrorResponse> handlePremium(
            PremiumOutOfRangeException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Premium Out Of Range", ex.getMessage(), req);
    }

    // ── 422 Unprocessable — policy not active ────────────────────────────
    @ExceptionHandler(PolicyNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleNotActive(
            PolicyNotActiveException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Policy Not Active", ex.getMessage(), req);
    }

    // ── 400 Bad Request — missing required field ─────────────────────────
    @ExceptionHandler(MissingRequiredFieldException.class)
    public ResponseEntity<ErrorResponse> handleMissingField(
            MissingRequiredFieldException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Missing Required Field", ex.getMessage(), req);
    }

    // ── 400 Bad Request — invalid field value ────────────────────────────
    @ExceptionHandler(InvalidFieldValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalidField(
            InvalidFieldValueException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Invalid Field Value", ex.getMessage(), req);
    }

    // ── 400 Bad Request — invalid operation ──────────────────────────────
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(
            InvalidOperationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Invalid Operation", ex.getMessage(), req);
    }

    // ── 400 Bad Request — @Valid annotation failures ─────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            fieldErrors.put(err.getField(), err.getDefaultMessage())
        );

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

    // ── 400 Bad Request — wrong type for @RequestParam / @PathVariable ───
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {

        String expected = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName() : "unknown";

        String message = String.format(
            "Parameter '%s' has invalid value '%s'. Expected type: %s.",
            ex.getName(), ex.getValue(), expected
        );
        return build(HttpStatus.BAD_REQUEST, "Type Mismatch", message, req);
    }

    // ── 400 Bad Request — missing @RequestParam entirely ─────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {

        String message = String.format(
            "Required request parameter '%s' of type '%s' is missing.",
            ex.getParameterName(), ex.getParameterType()
        );
        return build(HttpStatus.BAD_REQUEST, "Missing Parameter", message, req);
    }

    // ── 400 Bad Request — unreadable / malformed JSON body ───────────────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        String message = "Request body is missing or malformed. " +
                         "Check JSON syntax and field types.";
        return build(HttpStatus.BAD_REQUEST, "Malformed Request Body", message, req);
    }

    // ── 403 Forbidden — @PreAuthorize failures ───────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {

        return build(HttpStatus.FORBIDDEN,
            "Access Denied",
            "You do not have permission to access this resource. " +
            "Required role is missing or insufficient.",
            req);
    }

    // ── 401 Unauthorized — no/invalid auth headers ───────────────────────
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            AuthenticationException ex, HttpServletRequest req) {

        return build(HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "Authentication is required. Please provide a valid JWT token.",
            req);
    }

    // ── 500 fallback ─────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {

        return build(HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred: " + ex.getMessage(),
            req);
    }

    // ── builder helper ────────────────────────────────────────────────────
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