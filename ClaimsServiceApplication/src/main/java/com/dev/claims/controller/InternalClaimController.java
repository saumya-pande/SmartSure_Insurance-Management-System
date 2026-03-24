package com.dev.claims.controller;

import com.dev.claims.dto.ClaimResponse;
import com.dev.claims.entity.enums.ClaimStatus;
import com.dev.claims.repository.ClaimRepository;
import com.dev.claims.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/claims")
public class InternalClaimController {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ClaimRepository claimRepository;

    /** Admin Service: get a claim by ID */
    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.getClaimById(claimId));
    }

    /** Admin Service: update claim status (approve/reject/under-review) */
    @PutMapping("/{claimId}/status")
    public ResponseEntity<ClaimResponse> updateClaimStatus(
            @PathVariable Long claimId,
            @RequestParam("status") ClaimStatus status,
            @RequestParam(value = "adminRemarks", required = false) String adminRemarks) {
        return ResponseEntity.ok(claimService.updateStatus(claimId, status, adminRemarks));
    }

    /** Admin Service: list all claims by status */
    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getClaimsByStatus(@RequestParam(required = false) ClaimStatus status) {
        List<ClaimResponse> result;
        if (status != null) {
            result = claimRepository.findByStatus(status).stream()
                    .map(claimService::convertToResponse)
                    .collect(Collectors.toList());
        } else {
            result = claimRepository.findAll().stream()
                    .map(claimService::convertToResponse)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(result);
    }

    /** Admin Service: counts by status for dashboard */
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getClaimCounts() {
        return ResponseEntity.ok(Map.of(
            "SUBMITTED",    (long) claimRepository.findByStatus(ClaimStatus.SUBMITTED).size(),
            "UNDER_REVIEW", (long) claimRepository.findByStatus(ClaimStatus.UNDER_REVIEW).size(),
            "APPROVED",     (long) claimRepository.findByStatus(ClaimStatus.APPROVED).size(),
            "REJECTED",     (long) claimRepository.findByStatus(ClaimStatus.REJECTED).size()
        ));
    }
}
