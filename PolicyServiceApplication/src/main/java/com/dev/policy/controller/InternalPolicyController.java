package com.dev.policy.controller;

import com.dev.policy.dto.PolicyResponse;
import com.dev.policy.entity.enums.PolicyStatus;
import com.dev.policy.service.PolicyPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/policies")
public class InternalPolicyController {

    @Autowired
    private PolicyPurchaseService policyPurchaseService;

    @PutMapping("/{id}/status")
    public ResponseEntity<PolicyResponse> updatePolicyStatus(
            @PathVariable Long id,
            @RequestParam("status") PolicyStatus status,
            @RequestParam(value = "customerEmail", required = false) String customerEmail) {
        return ResponseEntity.ok(policyPurchaseService.updatePolicyStatus(id, status, customerEmail));
    }
}
