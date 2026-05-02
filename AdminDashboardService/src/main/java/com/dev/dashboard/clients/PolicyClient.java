package com.dev.dashboard.clients;


import java.util.Map;

import com.dev.dashboard.dto.RestPage;
import com.dev.dashboard.entity.PolicyType;
import com.dev.dashboard.entity.PolicyStatus;
import com.dev.dashboard.entity.PurchaseStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.dev.dashboard.dto.BasicPolicyRequest;
import com.dev.dashboard.dto.BasicPolicyResponse;
import com.dev.dashboard.dto.CustomerPolicyResponse;



@FeignClient(name = "policy-service")
public interface PolicyClient {

    // basic policy CRUD
    @PostMapping("/api/admin/policies")
    BasicPolicyResponse createPolicy(
            @RequestBody BasicPolicyRequest request,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    @PutMapping("/api/admin/policies/{id}")
    BasicPolicyResponse updatePolicy(
            @PathVariable(name = "id") Long id,
            @RequestBody BasicPolicyRequest request,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    @DeleteMapping("/api/admin/policies/{id}")
    void deletePolicy(
            @PathVariable(name = "id") Long id,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    @PatchMapping("/api/admin/policies/{id}/status")
    BasicPolicyResponse updatePolicyStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") PolicyStatus status,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    @GetMapping("/api/admin/policies")
    RestPage<BasicPolicyResponse> getBasicPolicies(
            @RequestParam(name = "type", required = false) PolicyType type,
            @RequestParam(name = "status", required = false) PolicyStatus status,
            @RequestParam(name = "policyName", required = false) String policyName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    // customer policies
    @GetMapping("/api/admin/policies/purchased")
    RestPage<CustomerPolicyResponse> getCustomerPolicies(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "policyType", required = false) PolicyType policyType,
            @RequestParam(name = "status", required = false) PurchaseStatus status,
            @RequestParam(name = "minPremium", required = false) Double minPremium,
            @RequestParam(name = "maxPremium", required = false) Double maxPremium,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    // dashboard counts
    @GetMapping("/api/admin/policies/count")
    Map<String, Long> getPolicyCounts(
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    @GetMapping("/api/admin/policies/revenue")
    Map<String, Double> getRevenue(
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );
}