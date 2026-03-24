package com.dev.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "policy-service")
public interface PolicyServiceAdminClient {

    @GetMapping("/internal/policies/pending")
    List<Object> getPendingPolicies();

    @GetMapping("/internal/policies")
    List<Object> getPoliciesByStatus(@RequestParam("status") String status);

    @GetMapping("/internal/policies/counts")
    Map<String, Long> getPolicyCounts();

    @PutMapping("/internal/policies/{policyId}/status")
    Object updatePolicyStatus(
        @PathVariable("policyId") Long policyId,
        @RequestParam("status") String status,
        @RequestParam(value = "customerEmail", required = false) String customerEmail
    );
}
