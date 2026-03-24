package com.dev.admin.controller;

import com.dev.admin.client.PolicyServiceAdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/policies")
public class AdminPolicyApprovalController {

    @Autowired
    private PolicyServiceAdminClient policyClient;

    @GetMapping("/pending")
    public ResponseEntity<List<Object>> getPendingPolicies() {
        return ResponseEntity.ok(policyClient.getPendingPolicies());
    }

    @GetMapping
    public ResponseEntity<List<Object>> getPoliciesByStatus(@RequestParam String status) {
        return ResponseEntity.ok(policyClient.getPoliciesByStatus(status));
    }

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getPolicyCounts() {
        return ResponseEntity.ok(policyClient.getPolicyCounts());
    }

    @PutMapping("/{policyId}/approve")
    public ResponseEntity<Object> approvePolicy(
            @PathVariable Long policyId,
            @RequestParam(required = false) String customerEmail) {
        return ResponseEntity.ok(policyClient.updatePolicyStatus(policyId, "ACTIVE", customerEmail));
    }

    @PutMapping("/{policyId}/reject")
    public ResponseEntity<Object> rejectPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(policyClient.updatePolicyStatus(policyId, "CANCELLED", null));
    }
}
