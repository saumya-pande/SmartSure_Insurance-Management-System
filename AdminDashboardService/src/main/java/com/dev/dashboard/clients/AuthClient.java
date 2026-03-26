package com.dev.dashboard.clients;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.dev.dashboard.clients.fallback.AuthClientFallback;
import com.dev.dashboard.dto.KycResponse;
import com.dev.dashboard.dto.UserResponse;
import com.dev.dashboard.entity.KycStatus;
import com.dev.dashboard.entity.Role;

@FeignClient(name = "AUTH-SERVICE", fallback = AuthClientFallback.class)
public interface AuthClient {

    // users
    @GetMapping("/api/admin/users")
    Page<UserResponse> getUsers(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestHeader("X-User-Role") String userRole
    );

    @PatchMapping("/api/admin/users/{id}/status")
    UserResponse toggleUserStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "active") boolean active,
            @RequestHeader("X-User-Role") String userRole
    );

    // kyc
    @GetMapping("/api/admin/kyc")
    Page<KycResponse> getKyc(
            @RequestParam(name = "status", required = false) KycStatus status,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestHeader("X-User-Role") String userRole
    );

    @PatchMapping("/api/admin/kyc/{id}/status")
    KycResponse updateKycStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") KycStatus status,
            @RequestHeader("X-User-Role") String userRole
    );

    // dashboard counts
    @GetMapping("/api/admin/users/count")
    Map<String, Long> getUserCounts(@RequestHeader("X-User-Role") String userRole);

    @GetMapping("/api/admin/kyc/count")
    Map<String, Long> getKycCounts(@RequestHeader("X-User-Role") String userRole);
}
