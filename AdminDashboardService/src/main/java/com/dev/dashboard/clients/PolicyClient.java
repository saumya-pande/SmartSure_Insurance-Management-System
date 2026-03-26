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
            @PathVariable(name = "id") Long id,
            @RequestBody BasicPolicyRequest request,
            @RequestHeader("X-User-Role") String userRole
    );

    @DeleteMapping("/api/admin/policies/{id}")
    void deletePolicy(
            @PathVariable(name = "id") Long id,
            @RequestHeader("X-User-Role") String userRole
    );

    @PatchMapping("/api/admin/policies/{id}/status")
    BasicPolicyResponse updatePolicyStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") com.dev.dashboard.entity.PolicyStatus status,
            @RequestHeader("X-User-Role") String userRole
    );

    @GetMapping("/api/admin/policies")
    Page<BasicPolicyResponse> getBasicPolicies(
            @RequestParam(name = "type", required = false) PolicyType type,
            @RequestParam(name = "status", required = false) com.dev.dashboard.entity.PolicyStatus status,
            @RequestParam(name = "policyName", required = false) String policyName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestHeader("X-User-Role") String userRole
    );

    // customer policies
    @GetMapping("/api/admin/policies/purchased")
    Page<CustomerPolicyResponse> getCustomerPolicies(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "policyType", required = false) com.dev.dashboard.entity.PolicyType policyType,
            @RequestParam(name = "status", required = false) com.dev.dashboard.entity.PurchaseStatus status,
            @RequestParam(name = "minPremium", required = false) Double minPremium,
            @RequestParam(name = "maxPremium", required = false) Double maxPremium,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestHeader("X-User-Role") String userRole
    );

    // dashboard counts
    @GetMapping("/api/admin/policies/count")
    Map<String, Long> getPolicyCounts(@RequestHeader("X-User-Role") String userRole);

    @GetMapping("/api/admin/policies/revenue")
    Map<String, Double> getRevenue(@RequestHeader("X-User-Role") String userRole);
}