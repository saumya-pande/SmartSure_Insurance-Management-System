package com.dev.authentication.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.dev.authentication.dto.KycAdminResponse;
import com.dev.authentication.dto.UserResponse;
import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.entity.Role;
import com.dev.authentication.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Users & KYC", description = "User management and KYC administration")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Get all users with optional filters")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getUsers(email, name, role, active, page, size));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Suspend or activate a user")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable Long id,
                                    @RequestParam boolean active) {
        return ResponseEntity.ok(adminService.toggleStatus(id, active));
    }

    @GetMapping("/users/count")
    @Operation(summary = "Get total, active, and suspended user counts")
    public ResponseEntity<Map<String, Long>> getUserCounts() {
        return ResponseEntity.ok(adminService.getUserCounts());
    }

    @GetMapping("/kyc")
    @Operation(summary = "Get all KYC submissions with optional filters")
    public ResponseEntity<Page<KycAdminResponse>> getKyc(
            @RequestParam(required = false) KycStatus status,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getKyc(status, email, page, size));
    }

    @PatchMapping("/kyc/{id}/status")
    @Operation(summary = "Approve or reject a KYC submission")
    public ResponseEntity<KycAdminResponse> updateKycStatus(@PathVariable Long id,
                                       @RequestParam KycStatus status) {
        return ResponseEntity.ok(adminService.updateKycStatus(id, status));
    }

    @GetMapping("/kyc/count")
    @Operation(summary = "Get KYC counts by status")
    public ResponseEntity<Map<String, Long>> getKycCounts() {
        return ResponseEntity.ok(adminService.getKycCounts());
    }
}