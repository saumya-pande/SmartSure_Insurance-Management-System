package com.dev.claims.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "policy-service")
public interface PolicyServiceClient {

    @GetMapping("/internal/policies/{policyId}/status")
    String getPolicyStatus(@PathVariable("policyId") Long policyId);
}
