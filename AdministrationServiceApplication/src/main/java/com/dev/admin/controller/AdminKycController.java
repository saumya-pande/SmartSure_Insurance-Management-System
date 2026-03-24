package com.dev.admin.controller;

import com.dev.admin.client.AuthServiceAdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/kyc")
public class AdminKycController {

    @Autowired
    private AuthServiceAdminClient authClient;

    @GetMapping("/pending")
    public ResponseEntity<List<Object>> getPendingKycUsers() {
        return ResponseEntity.ok(authClient.getKycPendingUsers());
    }

    @GetMapping("/users")
    public ResponseEntity<List<Object>> getUsersByKycStatus(@RequestParam String status) {
        return ResponseEntity.ok(authClient.getUsersByKycStatus(status));
    }

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getKycCounts() {
        return ResponseEntity.ok(authClient.getKycCounts());
    }

    @PutMapping("/{userId}/approve")
    public ResponseEntity<Object> approveKyc(@PathVariable Long userId) {
        return ResponseEntity.ok(authClient.updateKycStatus(userId, "VERIFIED"));
    }

    @PutMapping("/{userId}/reject")
    public ResponseEntity<Object> rejectKyc(@PathVariable Long userId) {
        return ResponseEntity.ok(authClient.updateKycStatus(userId, "REJECTED"));
    }
}
