package com.dev.policy.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.dev.policy.dto.BasicPolicyRequest;
import com.dev.policy.dto.BasicPolicyResponse;
import com.dev.policy.dto.CustomerPolicyResponse;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.entity.PurchaseStatus;
import com.dev.policy.service.BasicPolicyService;
import com.dev.policy.service.CustomerPolicyService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPolicyController {

    private final BasicPolicyService basicPolicyService;
    private final CustomerPolicyService customerPolicyService;

    @GetMapping("/policies/count")
    public Map<String, Object> getPolicyCounts() {
        Map<String, Long> basicCounts = basicPolicyService.getPolicyCounts();
        Map<String, Long> customerCounts = customerPolicyService.getCustomerPolicyCounts();

        return Map.of(
            "total",       basicCounts.get("total"),
            "ACTIVE",      basicCounts.get("ACTIVE"),
            "INACTIVE",    basicCounts.get("INACTIVE"),
            "basicHOME",   basicCounts.get("HOME"),
            "basicVEHICLE",basicCounts.get("VEHICLE"),
            "sold",        customerCounts.get("sold"),
            "soldActive",  customerCounts.get("soldActive"),
            "soldHOME",    customerCounts.get("HOME"),
            "soldVEHICLE", customerCounts.get("VEHICLE")
        );
    }

    @GetMapping("/policies/revenue")
    public Map<String, Double> getRevenue() {
        return customerPolicyService.getRevenue();
    }

    @PostMapping("/policies")
    public ResponseEntity<BasicPolicyResponse> create(
            @Valid @RequestBody BasicPolicyRequest r) {
        return ResponseEntity.ok(basicPolicyService.create(r));
    }

    @PutMapping("/policies/{id}")
    public ResponseEntity<BasicPolicyResponse> update(
            @PathVariable(name = "id") Long id, @Valid @RequestBody BasicPolicyRequest r) {
        return ResponseEntity.ok(basicPolicyService.update(id, r));
    }

    @DeleteMapping("/policies/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        basicPolicyService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/policies/{id}/status")
    public ResponseEntity<BasicPolicyResponse> updateStatus(
            @PathVariable(name = "id") Long id, @RequestParam(name = "status") PolicyStatus status) {
        return ResponseEntity.ok(basicPolicyService.updateStatus(id, status));
    }

    @GetMapping("/policies")
    public ResponseEntity<Page<BasicPolicyResponse>> getAll(
            @RequestParam(name = "status", required = false) PolicyStatus status,
            @RequestParam(name = "type", required = false) PolicyType type,
            @RequestParam(name = "policyName", required = false) String policyName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(
                basicPolicyService.getAll(status, type, policyName, PageRequest.of(page, size, Sort.by(sortBy))));
    }

    @GetMapping("/policies/purchased")
    public ResponseEntity<Page<CustomerPolicyResponse>> getPurchased(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "policyType", required = false) PolicyType policyType,
            @RequestParam(name = "status", required = false) PurchaseStatus purchaseStatus,
            @RequestParam(name = "minPremium", required = false) Double minPremium,
            @RequestParam(name = "maxPremium", required = false) Double maxPremium,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(
                customerPolicyService.getAll(email, policyType, purchaseStatus,
                        minPremium, maxPremium, startDate, endDate,
                        PageRequest.of(page, size, Sort.by(sortBy))));
    }
}