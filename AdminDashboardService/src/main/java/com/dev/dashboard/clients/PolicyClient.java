package com.dev.dashboard.clients;


import java.util.Map;

import com.dev.dashboard.clients.fallback.PolicyClientFallback;
import com.dev.dashboard.entity.PolicyType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.dev.dashboard.dto.BasicPolicyRequest;
import com.dev.dashboard.dto.BasicPolicyResponse;
import com.dev.dashboard.dto.CustomerPolicyResponse;



@FeignClient(name = "POLICY-SERVICE", fallback = PolicyClientFallback.class)
public interface PolicyClient {

    // basic policy CRUD
    @PostMapping("/api/admin/policies")
    BasicPolicyResponse createPolicy(
            @RequestBody BasicPolicyRequest request,
            @RequestHeader("X-User-Role") String userRole
    );

    @PutMapping("/api/admin/policies/{id}")
    BasicPolicyResponse updatePolicy(
            @PathVariable Long id,
            @RequestBody BasicPolicyRequest request,
            @RequestHeader("X-User-Role") String userRole
    );

    @DeleteMapping("/api/admin/policies/{id}")
    void deletePolicy(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String userRole
    );

    @PatchMapping("/api/admin/policies/{id}/status")
    BasicPolicyResponse updatePolicyStatus(
            @PathVariable Long id,
            @RequestParam com.dev.dashboard.entity.PolicyStatus status,
            @RequestHeader("X-User-Role") String userRole
    );

    @GetMapping("/api/admin/policies")
    Page<BasicPolicyResponse> getBasicPolicies(
            @RequestParam(required = false) PolicyType type,
            @RequestParam(required = false) com.dev.dashboard.entity.PolicyStatus status,
            @RequestParam(required = false) String policyName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestHeader("X-User-Role") String userRole
    );

    // customer policies
    @GetMapping("/api/admin/policies/purchased")
    Page<CustomerPolicyResponse> getCustomerPolicies(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) com.dev.dashboard.entity.PolicyType policyType,
            @RequestParam(required = false) com.dev.dashboard.entity.PurchaseStatus status,
            @RequestParam(required = false) Double minPremium,
            @RequestParam(required = false) Double maxPremium,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestHeader("X-User-Role") String userRole
    );

    // dashboard counts
    @GetMapping("/api/admin/policies/count")
    Map<String, Long> getPolicyCounts(@RequestHeader("X-User-Role") String userRole);

    @GetMapping("/api/admin/policies/revenue")
    Map<String, Double> getRevenue(@RequestHeader("X-User-Role") String userRole);
}