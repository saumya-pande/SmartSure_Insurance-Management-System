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
            @RequestParam Long customerPolicyId,
            @RequestParam Double claimAmount,
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
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.submit(email, id));
    }

    // CUSTOMER — get my claims
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    @Operation(summary = "Get my claims")
    public ResponseEntity<Page<ClaimResponse>> getMyClaims(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getMyClaims(email,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    // ADMIN — start review
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/review")
    @Operation(summary = "Move claim to UNDER_REVIEW (admin)")
    public ResponseEntity<ClaimResponse> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(service.startReview(id));
    }

    // ADMIN — approve or reject
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Approve or Reject a claim (admin)")
    public ResponseEntity<ClaimResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ClaimStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    // ADMIN — close claim
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/close")
    @Operation(summary = "Close a claim (admin)")
    public ResponseEntity<ClaimResponse> close(@PathVariable Long id) {
        return ResponseEntity.ok(service.close(id));
    }

    // ADMIN — get all claims
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Get all claims (admin)")
    public ResponseEntity<Page<ClaimResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}