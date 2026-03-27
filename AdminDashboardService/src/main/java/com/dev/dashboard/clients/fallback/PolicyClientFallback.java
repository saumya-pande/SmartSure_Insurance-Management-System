package com.dev.dashboard.clients.fallback;

import com.dev.dashboard.clients.PolicyClient;
import com.dev.dashboard.dto.BasicPolicyRequest;
import com.dev.dashboard.dto.BasicPolicyResponse;
import com.dev.dashboard.dto.CustomerPolicyResponse;
import com.dev.dashboard.entity.PolicyStatus;
import com.dev.dashboard.entity.PolicyType;
import com.dev.dashboard.entity.PurchaseStatus;
import com.dev.dashboard.exception.ServiceUnavailableException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PolicyClientFallback implements PolicyClient {

    private static final String SERVICE_NAME = "Policy Service";

    @Override
    public BasicPolicyResponse createPolicy(BasicPolicyRequest request, String userRole, String userEmail) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public BasicPolicyResponse updatePolicy(Long id, BasicPolicyRequest request, String userRole, String userEmail) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void deletePolicy(Long id, String userRole, String userEmail) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public BasicPolicyResponse updatePolicyStatus(Long id, PolicyStatus status, String userRole, String userEmail) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Page<BasicPolicyResponse> getBasicPolicies(PolicyType type, PolicyStatus status,
            String policyName, int page, int size, String sortBy, String userRole, String userEmail) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Page<CustomerPolicyResponse> getCustomerPolicies(String email, PolicyType policyType,
            PurchaseStatus status, Double minPremium, Double maxPremium,
            String startDate, String endDate, int page, int size, String sortBy, String userRole, String userEmail) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Map<String, Long> getPolicyCounts(String userRole, String userEmail) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Map<String, Double> getRevenue(String userRole, String userEmail) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}
