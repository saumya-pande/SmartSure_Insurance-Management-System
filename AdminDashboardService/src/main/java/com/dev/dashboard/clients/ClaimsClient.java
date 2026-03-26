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

    @GetMapping("/api/admin/claims")
    Page<ClaimResponse> getClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Role") String userRole
    );

    @PatchMapping("/api/admin/claims/{id}/status")
    ClaimResponse overrideClaimStatus(
            @PathVariable Long id,
            @RequestParam ClaimStatus status,
            @RequestHeader("X-User-Role") String userRole
    );

    @GetMapping("/api/admin/claims/count")
    Map<String, Long> getClaimCounts(@RequestHeader("X-User-Role") String userRole);
}