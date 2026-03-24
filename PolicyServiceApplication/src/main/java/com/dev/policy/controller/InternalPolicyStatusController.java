package com.dev.policy.controller;

import com.dev.policy.entity.enums.PolicyStatus;
import com.dev.policy.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal-only endpoints for Feign clients (Claims Service).
 * Not routed through the API Gateway.
 */
@RestController
@RequestMapping("/internal/policies")
public class InternalPolicyStatusController {

    @Autowired
    private PolicyRepository policyRepository;

    @GetMapping("/{policyId}/status")
    public ResponseEntity<String> getPolicyStatus(@PathVariable Long policyId) {
        return policyRepository.findById(policyId)
                .map(p -> ResponseEntity.ok(p.getStatus().name()))
                .orElse(ResponseEntity.notFound().build());
    }
}
