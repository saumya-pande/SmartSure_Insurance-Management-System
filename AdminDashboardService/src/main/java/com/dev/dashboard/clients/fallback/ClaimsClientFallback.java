package com.dev.dashboard.clients.fallback;

import com.dev.dashboard.clients.ClaimsClient;
import com.dev.dashboard.dto.ClaimResponse;
import com.dev.dashboard.entity.ClaimStatus;
import com.dev.dashboard.exception.ServiceUnavailableException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ClaimsClientFallback implements ClaimsClient {

    private static final String SERVICE_NAME = "Claims Service";

    @Override
    public Page<ClaimResponse> getClaims(ClaimStatus status, String email,
            String startDate, String endDate, int page, int size, String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public ClaimResponse overrideClaimStatus(Long id, ClaimStatus status, String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Map<String, Long> getClaimCounts(String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Map<String, Double> getPayouts(String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}
