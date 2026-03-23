package com.dev.policy.controller;

import com.dev.policy.dto.PolicyResponse;
import com.dev.policy.dto.PolicyTypeResponse;
import com.dev.policy.dto.PurchasePolicyRequest;
import com.dev.policy.service.PolicyPurchaseService;
import com.dev.policy.service.PolicyTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class CustomerPolicyController {

    @Autowired
    private PolicyTypeService typeService;

    @Autowired
    private PolicyPurchaseService purchaseService;

    @GetMapping("/types")
    public ResponseEntity<List<PolicyTypeResponse>> browseAvailablePolicies() {
        return ResponseEntity.ok(typeService.getAllPolicyTypes());
    }

    @GetMapping("/types/{id}")
    public ResponseEntity<PolicyTypeResponse> getPolicyType(@PathVariable Long id) {
        return ResponseEntity.ok(typeService.getPolicyTypeById(id));
    }

    @PostMapping("/purchase")
    public ResponseEntity<PolicyResponse> purchasePolicy(
            @Valid @RequestBody PurchasePolicyRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-LoggedIn-User") String email) {
        return ResponseEntity.ok(purchaseService.purchasePolicy(request, userId, email));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PolicyResponse>> getMyPolicies(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(purchaseService.getMyPolicies(userId));
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyResponse> getPolicyDetails(@PathVariable Long policyId) {
        return ResponseEntity.ok(purchaseService.getPolicyById(policyId));
    }
}
