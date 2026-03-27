package com.dev.claimsservice.controller;

import com.dev.claimsservice.dto.*;
import com.dev.claimsservice.entity.ClaimStatus;
import com.dev.claimsservice.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService service;

    // CUSTOMER — create draft claim + upload documents
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping(value = "/draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a draft claim with documents")
    public ResponseEntity<ClaimResponse> createDraft(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @RequestParam(name = "customerPolicyId") Long customerPolicyId,
            @RequestParam(name = "claimAmount") Double claimAmount,
            @RequestPart("files") List<MultipartFile> files
    ) throws Exception {
        ClaimRequest request = new ClaimRequest();
        request.setCustomerPolicyId(customerPolicyId);
        request.setClaimAmount(claimAmount);
        return ResponseEntity.ok(service.createDraft(email, request, files));
    }

    // CUSTOMER — submit draft
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/{id}/submit")
    @Operation(summary = "Submit a draft claim")
    public ResponseEntity<ClaimResponse> submit(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(service.submit(email, id));
    }

    // CUSTOMER — get my claims
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    @Operation(summary = "Get my claims")
    public ResponseEntity<Page<ClaimResponse>> getMyClaims(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getMyClaims(email,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    // ADMIN — approve or reject or review or close
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update claim status (admin)")
    public ResponseEntity<ClaimResponse> updateStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") ClaimStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    // ADMIN — get all claims
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Get all claims (admin) with optional status filter")
    public ResponseEntity<Page<ClaimResponse>> getAll(
            @RequestParam(name = "status", required = false) ClaimStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getAll(
                status,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    // ADMIN — get claim counts
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count")
    @Operation(summary = "Get claim counts by status (admin)")
    public ResponseEntity<Map<String, Long>> getClaimCounts() {
        return ResponseEntity.ok(service.getClaimCounts());
    }

    // ADMIN — get total approved payouts
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payouts")
    @Operation(summary = "Get total approved claim payouts (admin)")
    public ResponseEntity<Map<String, Double>> getApprovedPayouts() {
        return ResponseEntity.ok(service.getApprovedPayouts());
    }
}