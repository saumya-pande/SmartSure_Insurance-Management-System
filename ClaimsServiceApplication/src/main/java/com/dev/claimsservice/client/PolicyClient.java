package com.dev.claimsservice.client;

import com.dev.claimsservice.dto.CustomerPolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "POLICY-SERVICE", fallback = PolicyClientFallback.class)
public interface PolicyClient {

    @GetMapping("/api/policies/purchase/purchase/{id}")
    CustomerPolicyResponse getPolicyById(
            @PathVariable(name = "id") Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    );
}