package com.dev.policy.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/internal/auth/users/{userId}/kyc-status")
    String getUserKycStatus(@PathVariable("userId") Long userId);
}
