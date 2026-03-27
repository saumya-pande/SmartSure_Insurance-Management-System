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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dev.policy.dto.BasicPolicyRequest;
import com.dev.policy.dto.BasicPolicyResponse;
import com.dev.policy.dto.CustomerPolicyResponse;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.service.BasicPolicyService;
import com.dev.policy.service.CustomerPolicyService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
            "sold",        customerCounts.get("sold"),
            "soldActive",  customerCounts.get("soldActive"),
            "HOME",        customerCounts.get("HOME"),
            "VEHICLE",     customerCounts.get("VEHICLE")
        );
    }

    @GetMapping("/policies/revenue")
    public Map<String, Double> getRevenue() {
        return customerPolicyService.getRevenue();
    }

    @PostMapping("/policies")
    public ResponseEntity<BasicPolicyResponse> create(
            @org.springframework.web.bind.annotation.RequestBody BasicPolicyRequest r) {
        return ResponseEntity.ok(basicPolicyService.create(r));
    }

    @PutMapping("/policies/{id}")
    public ResponseEntity<BasicPolicyResponse> update(
            @PathVariable(name = "id") Long id, @org.springframework.web.bind.annotation.RequestBody BasicPolicyRequest r) {
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
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(
                basicPolicyService.getAll(PageRequest.of(page, size, Sort.by(sortBy))));
    }

    @GetMapping("/policies/purchased")
    public ResponseEntity<Page<CustomerPolicyResponse>> getPurchased(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(
                customerPolicyService.getAll(PageRequest.of(page, size, Sort.by(sortBy))));
    }
}