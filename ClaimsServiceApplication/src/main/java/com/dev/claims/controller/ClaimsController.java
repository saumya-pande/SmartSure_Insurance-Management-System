package com.dev.claims.controller;

import com.dev.claims.dto.ClaimResponse;
import com.dev.claims.dto.InitiateClaimRequest;
import com.dev.claims.entity.enums.ClaimDocumentType;
import com.dev.claims.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimsController {

    @Autowired
    private ClaimService claimService;

    /** Step 1: Customer initiates a new claim */
    @PostMapping
    public ResponseEntity<ClaimResponse> initiateClaim(
            @Valid @RequestBody InitiateClaimRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-LoggedIn-User") String userEmail) {
        return new ResponseEntity<>(claimService.initiateClaim(request, userId, userEmail), HttpStatus.CREATED);
    }

    /** Step 2: Customer uploads incident documents one at a time */
    @PostMapping("/{claimId}/documents")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long claimId,
            @RequestParam("documentType") ClaimDocumentType type,
            @RequestParam("file") MultipartFile file) {
        try {
            claimService.uploadDocument(claimId, type, file);
            return ResponseEntity.ok("Document " + type + " uploaded for claim " + claimId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Get a single claim by ID */
    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.getClaimById(claimId));
    }

    /** Get all claims for the logged-in customer */
    @GetMapping("/my")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(claimService.getMyClaims(userId));
    }
}
