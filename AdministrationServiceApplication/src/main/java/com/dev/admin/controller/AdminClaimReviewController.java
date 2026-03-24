package com.dev.admin.controller;

import com.dev.admin.client.ClaimsServiceAdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/claims")
public class AdminClaimReviewController {

    @Autowired
    private ClaimsServiceAdminClient claimsClient;

    @GetMapping("/pending")
    public ResponseEntity<List<Object>> getPendingClaims() {
        return ResponseEntity.ok(claimsClient.getClaimsByStatus("SUBMITTED"));
    }

    @GetMapping("/approved")
    public ResponseEntity<List<Object>> getApprovedClaims() {
        return ResponseEntity.ok(claimsClient.getClaimsByStatus("APPROVED"));
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<Object>> getRejectedClaims() {
        return ResponseEntity.ok(claimsClient.getClaimsByStatus("REJECTED"));
    }

    @GetMapping
    public ResponseEntity<List<Object>> getAllClaims(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(claimsClient.getClaimsByStatus(status));
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<Object> getClaimById(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimsClient.getClaimById(claimId));
    }

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getClaimCounts() {
        return ResponseEntity.ok(claimsClient.getClaimCounts());
    }

    @PutMapping("/{claimId}/begin-review")
    public ResponseEntity<Object> startReview(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimsClient.updateClaimStatus(claimId, "UNDER_REVIEW", null));
    }

    @PutMapping("/{claimId}/approve")
    public ResponseEntity<Object> approveClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimsClient.updateClaimStatus(claimId, "APPROVED", null));
    }

    @PutMapping("/{claimId}/reject")
    public ResponseEntity<Object> rejectClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String adminRemarks) {
        return ResponseEntity.ok(claimsClient.updateClaimStatus(claimId, "REJECTED", adminRemarks));
    }
}
