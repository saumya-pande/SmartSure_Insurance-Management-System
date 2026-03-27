package com.dev.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.dev.dashboard.dto.BasicPolicyRequest;
import com.dev.dashboard.dto.BasicPolicyResponse;
import com.dev.dashboard.dto.ClaimResponse;
import com.dev.dashboard.dto.CustomerPolicyResponse;
import com.dev.dashboard.dto.DashboardResponse;
import com.dev.dashboard.dto.KycResponse;
import com.dev.dashboard.dto.UserResponse;
import com.dev.dashboard.entity.ClaimStatus;
import com.dev.dashboard.entity.KycStatus;
import com.dev.dashboard.entity.PolicyStatus;
import com.dev.dashboard.entity.PolicyType;
import com.dev.dashboard.entity.PurchaseStatus;
import com.dev.dashboard.entity.Role;
import com.dev.dashboard.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // all endpoints admin only
public class AdminController {

    private final AdminDashboardService service;

    // ── Dashboard ─────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get full dashboard summary")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(service.getDashboard());
    }

    // ── Users ─────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "Get all users with filters")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getUsers(email, name, role, active, page, size));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Suspend or activate a user")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "active") boolean active
    ) {
        return ResponseEntity.ok(service.toggleUserStatus(id, active));
    }

    // ── KYC ───────────────────────────────────────────────────

    @GetMapping("/kyc")
    @Operation(summary = "Get all KYC submissions with filters")
    public ResponseEntity<Page<KycResponse>> getKyc(
            @RequestParam(name = "status", required = false) KycStatus status,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getKyc(status, email, page, size));
    }

    @PatchMapping("/kyc/{id}/status")
    @Operation(summary = "Update KYC status")
    public ResponseEntity<KycResponse> updateKycStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") KycStatus status
    ) {
        return ResponseEntity.ok(service.updateKycStatus(id, status));
    }

    // ── Basic Policies ────────────────────────────────────────

    @PostMapping("/policies")
    @Operation(summary = "Create basic policy")
    public ResponseEntity<BasicPolicyResponse> createPolicy(
            @Valid @RequestBody BasicPolicyRequest request
    ) {
        return ResponseEntity.ok(service.createPolicy(request));
    }

    @PutMapping("/policies/{id}")
    @Operation(summary = "Update basic policy")
    public ResponseEntity<BasicPolicyResponse> updatePolicy(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody BasicPolicyRequest request
    ) {
        return ResponseEntity.ok(service.updatePolicy(id, request));
    }

    @DeleteMapping("/policies/{id}")
    @Operation(summary = "Delete basic policy")
    public ResponseEntity<String> deletePolicy(@PathVariable(name = "id") Long id) {
        service.deletePolicy(id);
        return ResponseEntity.ok("Policy deleted");
    }

    @PatchMapping("/policies/{id}/status")
    @Operation(summary = "Update basic policy status")
    public ResponseEntity<BasicPolicyResponse> updatePolicyStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") PolicyStatus status
    ) {
        return ResponseEntity.ok(service.updatePolicyStatus(id, status));
    }

    @GetMapping("/policies")
    @Operation(summary = "Get basic policies with filters")
    public ResponseEntity<Page<BasicPolicyResponse>> getBasicPolicies(
            @RequestParam(name = "type", required = false) PolicyType type,
            @RequestParam(name = "status", required = false) PolicyStatus status,
            @RequestParam(name = "policyName", required = false) String policyName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(
                service.getBasicPolicies(type, status, policyName, page, size, sortBy));
    }

    // ── Customer Policies ─────────────────────────────────────

    @GetMapping("/policies/purchased")
    @Operation(summary = "Get customer policies with filters")
    public ResponseEntity<Page<CustomerPolicyResponse>> getCustomerPolicies(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "policyType", required = false) PolicyType policyType,
            @RequestParam(name = "status", required = false) PurchaseStatus status,
            @RequestParam(name = "minPremium", required = false) Double minPremium,
            @RequestParam(name = "maxPremium", required = false) Double maxPremium,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(service.getCustomerPolicies(
                email, policyType, status, minPremium, maxPremium,
                startDate, endDate, page, size, sortBy));
    }

    // ── Claims ────────────────────────────────────────────────

    @GetMapping("/claims")
    @Operation(summary = "Get all claims with filters")
    public ResponseEntity<Page<ClaimResponse>> getClaims(
            @RequestParam(name = "status", required = false) ClaimStatus status,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                service.getClaims(status, email, startDate, endDate, page, size));
    }

    @PatchMapping("/claims/{id}/status")
    @Operation(summary = "Override claim status (admin)")
    public ResponseEntity<ClaimResponse> overrideClaimStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") ClaimStatus status
    ) {
        return ResponseEntity.ok(service.overrideClaimStatus(id, status));
    }
}
