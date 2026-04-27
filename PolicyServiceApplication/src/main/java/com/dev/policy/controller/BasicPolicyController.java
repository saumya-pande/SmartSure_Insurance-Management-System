package com.dev.policy.controller;


import com.dev.policy.dto.*;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.service.BasicPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class BasicPolicyController {

    private final BasicPolicyService service;

    // ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a policy (admin)")
    public ResponseEntity<BasicPolicyResponse> create(@Valid @RequestBody BasicPolicyRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a policy (admin)")
    public ResponseEntity<BasicPolicyResponse> update(@PathVariable(name = "id") Long id,
            @Valid @RequestBody BasicPolicyRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a policy (admin)")
    public ResponseEntity<String> delete(@PathVariable(name = "id") Long id) {
        service.delete(id);
        return ResponseEntity.ok("Policy deleted");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update policy status (admin)")
    public ResponseEntity<BasicPolicyResponse> updateStatus(@PathVariable(name = "id") Long id,
            @RequestParam(name = "status") PolicyStatus status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    @Operation(summary = "Get all policies (admin)")
    public ResponseEntity<Page<BasicPolicyResponse>> getAll(
            @RequestParam(name = "status", required = false) PolicyStatus status,
            @RequestParam(name = "type", required = false) com.dev.policy.entity.PolicyType type,
            @RequestParam(name = "policyName", required = false) String policyName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(status, type, policyName, PageRequest.of(page, size, Sort.by("id"))));
    }

    // CUSTOMER — only sees active policies
    @GetMapping("/active")
    @Operation(summary = "Get active policies (customer)")
    public ResponseEntity<Page<BasicPolicyResponse>> getActive(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getActive(PageRequest.of(page, size, Sort.by("id"))));
    }
    
    @GetMapping("/active/{id}")
    @Operation(summary = "Get policy by id")
    public ResponseEntity<BasicPolicyResponse> getById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}