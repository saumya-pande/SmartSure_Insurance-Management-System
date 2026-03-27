package com.dev.policy.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", fallback = KycClientFallback.class)
public interface KycClient {

    @GetMapping("/api/kyc/my")
    KycResponse getMyKyc(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role);
}
