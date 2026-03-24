package com.dev.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthServiceAdminClient {

    @GetMapping("/internal/auth/users")
    List<Object> getAllUsers();

    @GetMapping("/internal/auth/users/{userId}")
    Object getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/internal/auth/users/kyc/pending")
    List<Object> getKycPendingUsers();

    @GetMapping("/internal/auth/users/kyc")
    List<Object> getUsersByKycStatus(@RequestParam("status") String status);

    @GetMapping("/internal/auth/users/kyc/counts")
    Map<String, Long> getKycCounts();

    @PutMapping("/internal/auth/kyc/{userId}/status")
    Object updateKycStatus(@PathVariable("userId") Long userId, @RequestParam("status") String status);
}
