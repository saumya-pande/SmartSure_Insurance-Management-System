package com.dev.policy.tests;


import com.dev.policy.entity.PolicyType;
import com.dev.policy.exception.DuplicatePropertyInsuranceException;
import com.dev.policy.exception.ErrorResponse;
import com.dev.policy.exception.GlobalExceptionHandler;
import com.dev.policy.exception.InvalidFieldValueException;
import com.dev.policy.exception.MissingRequiredFieldException;
import com.dev.policy.exception.PolicyNotActiveException;
import com.dev.policy.exception.PolicyNotFoundException;
import com.dev.policy.exception.PremiumOutOfRangeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/policies/test");
    }

    @Test
    @DisplayName("PolicyNotFoundException → 404 with correct message")
    void handleNotFound() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new PolicyNotFoundException(99L), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("99");
        assertThat(response.getBody().getPath()).isEqualTo("/api/policies/test");
    }

    @Test
    @DisplayName("DuplicatePropertyInsuranceException → 409 with HOME message")
    void handleDuplicate_home() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(
                new DuplicatePropertyInsuranceException("FLAT-101", PolicyType.HOME), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("FLAT-101");
        assertThat(response.getBody().getMessage()).contains("house/flat");
    }

    @Test
    @DisplayName("DuplicatePropertyInsuranceException → 409 with VEHICLE message")
    void handleDuplicate_vehicle() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(
                new DuplicatePropertyInsuranceException("MH-01-AB-1234", PolicyType.VEHICLE),
                request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("vehicle");
    }

    @Test
    @DisplayName("PremiumOutOfRangeException → 422 with range in message")
    void handlePremium() {
        ResponseEntity<ErrorResponse> response = handler.handlePremium(
                new PremiumOutOfRangeException(9999.0, 1000.0, 5000.0), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getMessage()).contains("9999");
        assertThat(response.getBody().getMessage()).contains("1000");
        assertThat(response.getBody().getMessage()).contains("5000");
    }

    @Test
    @DisplayName("PolicyNotActiveException → 422 with policy id")
    void handleNotActive() {
        ResponseEntity<ErrorResponse> response = handler.handleNotActive(
                new PolicyNotActiveException(1L), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getMessage()).contains("1");
    }

    @Test
    @DisplayName("MissingRequiredFieldException → 400 with field name")
    void handleMissingField() {
        ResponseEntity<ErrorResponse> response = handler.handleMissingField(
                new MissingRequiredFieldException("premiumAmount"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("premiumAmount");
    }

    @Test
    @DisplayName("InvalidFieldValueException → 400 with field and value")
    void handleInvalidField() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidField(
                new InvalidFieldValueException("basePremium", 9000.0,
                        "basePremium cannot be greater than maxPremium"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("basePremium");
    }

    @Test
    @DisplayName("AccessDeniedException → 403")
    void handleAccessDenied() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                new org.springframework.security.access.AccessDeniedException("denied"),
                request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError()).isEqualTo("Access Denied");
        assertThat(response.getBody().getMessage()).contains("permission");
    }

    @Test
    @DisplayName("Generic Exception → 500")
    void handleGeneric() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(
                new RuntimeException("something broke"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("something broke");
    }

    @Test
    @DisplayName("ErrorResponse timestamp should not be null")
    void errorResponse_hasTimestamp() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new PolicyNotFoundException(1L), request);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}