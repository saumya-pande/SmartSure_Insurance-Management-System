package com.dev.policy.controller;

import com.dev.policy.dto.PolicyResponse;
import com.dev.policy.entity.enums.PolicyStatus;
import com.dev.policy.mapper.PolicyMapper;
import com.dev.policy.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Internal endpoints consumed only by Admin Service via Feign.
 * Not routed through the API Gateway.
 */
@RestController
@RequestMapping("/internal/policies")
public class InternalPolicyQueryController {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyMapper policyMapper;

    /** Already in InternalPolicyStatusController — status by ID */

    /** Admin: all policies awaiting approval */
    @GetMapping("/pending")
    public ResponseEntity<List<PolicyResponse>> getPendingPolicies() {
        return ResponseEntity.ok(
            policyRepository.findByStatus(PolicyStatus.PENDING_APPROVAL).stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList())
        );
    }

    /** Admin: all policies by status */
    @GetMapping
    public ResponseEntity<List<PolicyResponse>> getPoliciesByStatus(@RequestParam PolicyStatus status) {
        return ResponseEntity.ok(
            policyRepository.findByStatus(status).stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList())
        );
    }

    /** Admin: count by status — for dashboard */
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getPolicyCounts() {
        return ResponseEntity.ok(Map.of(
            "PENDING_APPROVAL", (long) policyRepository.findByStatus(PolicyStatus.PENDING_APPROVAL).size(),
            "ACTIVE",           (long) policyRepository.findByStatus(PolicyStatus.ACTIVE).size(),
            "EXPIRED",          (long) policyRepository.findByStatus(PolicyStatus.EXPIRED).size(),
            "CANCELLED",        (long) policyRepository.findByStatus(PolicyStatus.CANCELLED).size()
        ));
    }
}
