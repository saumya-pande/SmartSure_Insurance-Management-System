package com.dev.policy.controller;

import com.dev.policy.dto.PolicyTypeRequest;
import com.dev.policy.dto.PolicyTypeResponse;
import com.dev.policy.service.PolicyTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/policies")
public class AdminPolicyController {

    @Autowired
    private PolicyTypeService policyTypeService;

    @PostMapping("/types")
    public ResponseEntity<PolicyTypeResponse> createPolicyProduct(@Valid @RequestBody PolicyTypeRequest request) {
        return new ResponseEntity<>(policyTypeService.createPolicyType(request), HttpStatus.CREATED);
    }

    @PutMapping("/types/{id}")
    public ResponseEntity<PolicyTypeResponse> updatePolicyProduct(@PathVariable Long id, @Valid @RequestBody PolicyTypeRequest request) {
        return ResponseEntity.ok(policyTypeService.updatePolicyType(id, request));
    }

    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deletePolicyProduct(@PathVariable Long id) {
        policyTypeService.deletePolicyType(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types")
    public ResponseEntity<List<PolicyTypeResponse>> getAllPolicyProducts() {
        return ResponseEntity.ok(policyTypeService.getAllPolicyTypes());
    }
}
