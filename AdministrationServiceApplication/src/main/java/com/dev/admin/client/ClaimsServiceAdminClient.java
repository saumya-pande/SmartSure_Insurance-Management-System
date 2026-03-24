package com.dev.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "claims-service")
public interface ClaimsServiceAdminClient {

    @GetMapping("/internal/claims/{claimId}")
    Object getClaimById(@PathVariable("claimId") Long claimId);

    @GetMapping("/internal/claims")
    List<Object> getClaimsByStatus(@RequestParam(value = "status", required = false) String status);

    @GetMapping("/internal/claims/counts")
    Map<String, Long> getClaimCounts();

    @PutMapping("/internal/claims/{claimId}/status")
    Object updateClaimStatus(
        @PathVariable("claimId") Long claimId,
        @RequestParam("status") String status,
        @RequestParam(value = "adminRemarks", required = false) String adminRemarks
    );
}
