package com.dev.policy.controller;

import com.dev.policy.dto.*;
import com.dev.policy.service.CustomerPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies/purchase")
@RequiredArgsConstructor
public class CustomerPolicyController {

    private final CustomerPolicyService service;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    @Operation(summary = "Purchase a policy")
    public ResponseEntity<CustomerPolicyResponse> purchase(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody PurchasePolicyRequest request) {
        return ResponseEntity.ok(service.purchase(email, role, request));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    @Operation(summary = "Get my purchased policies")
    public ResponseEntity<Page<CustomerPolicyResponse>> getMyPolicies(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getMyPolicies(email,
                PageRequest.of(page, size, Sort.by("id"))));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    @Operation(summary = "Get all purchased policies (admin)")
    public ResponseEntity<Page<CustomerPolicyResponse>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size, Sort.by("id"))));
    }

    // exposed to feign (admin for review, customer for claim creation)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/purchase/{id}")
    @Operation(summary = "Get purchased policy by id")
    public ResponseEntity<CustomerPolicyResponse> getById(
            @PathVariable(name = "id") Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(service.getById(id));
    }
}