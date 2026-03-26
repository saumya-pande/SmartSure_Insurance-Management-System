package com.dev.dashboard.clients.fallback;

import com.dev.dashboard.clients.AuthClient;
import com.dev.dashboard.dto.KycResponse;
import com.dev.dashboard.dto.UserResponse;
import com.dev.dashboard.entity.KycStatus;
import com.dev.dashboard.entity.Role;
import com.dev.dashboard.exception.ServiceUnavailableException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthClientFallback implements AuthClient {

    private static final String SERVICE_NAME = "Authentication Service";

    @Override
    public Page<UserResponse> getUsers(String email, String name, Role role,
            Boolean active, int page, int size, String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public UserResponse toggleUserStatus(Long id, boolean active, String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Page<KycResponse> getKyc(KycStatus status, String email,
            int page, int size, String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public KycResponse updateKycStatus(Long id, KycStatus status, String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Map<String, Long> getUserCounts(String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Map<String, Long> getKycCounts(String userRole) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}
