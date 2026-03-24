package com.dev.authservice.controller;

import com.dev.authservice.entity.KycStatus;
import com.dev.authservice.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/auth/kyc")
public class InternalKycController {

    @Autowired
    private KycService kycService;

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateKycStatus(
            @PathVariable Long userId,
            @RequestParam("status") KycStatus status) {
        try {
            String result = kycService.updateKycStatus(userId, status);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
