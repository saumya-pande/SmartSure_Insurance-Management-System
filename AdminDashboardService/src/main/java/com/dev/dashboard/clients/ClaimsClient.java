package com.dev.dashboard.clients;

import com.dev.dashboard.clients.fallback.ClaimsClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.dev.dashboard.dto.ClaimResponse;
import com.dev.dashboard.entity.ClaimStatus;

import java.util.Map;

@FeignClient(name = "CLAIMS-SERVICE", fallback = ClaimsClientFallback.class)
public interface ClaimsClient {

    @GetMapping("/api/claims")
    Page<ClaimResponse> getClaims(
            @RequestParam(name = "status", required = false) ClaimStatus status,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    @PatchMapping("/api/claims/{id}/status")
    ClaimResponse overrideClaimStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") ClaimStatus status,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    @GetMapping("/api/claims/count")
    Map<String, Long> getClaimCounts(
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );

    @GetMapping("/api/claims/payouts")
    Map<String, Double> getPayouts(
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Email") String userEmail
    );
}