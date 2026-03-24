package com.dev.admin.controller;

import com.dev.admin.client.AuthServiceAdminClient;
import com.dev.admin.client.ClaimsServiceAdminClient;
import com.dev.admin.client.PolicyServiceAdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AuthServiceAdminClient authClient;

    @Autowired
    private PolicyServiceAdminClient policyClient;

    @Autowired
    private ClaimsServiceAdminClient claimsClient;

    /**
     * Single-call dashboard endpoint — aggregates counts from all three services.
     * Returns KYC counts, policy counts, and claims counts in one response.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        try {
            dashboard.put("kycSummary", authClient.getKycCounts());
        } catch (Exception e) {
            dashboard.put("kycSummary", "Unavailable");
        }

        try {
            dashboard.put("policySummary", policyClient.getPolicyCounts());
        } catch (Exception e) {
            dashboard.put("policySummary", "Unavailable");
        }

        try {
            dashboard.put("claimsSummary", claimsClient.getClaimCounts());
        } catch (Exception e) {
            dashboard.put("claimsSummary", "Unavailable");
        }

        return ResponseEntity.ok(dashboard);
    }
}
