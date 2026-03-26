package com.dev.policy.service;

import com.dev.policy.dto.*;
import com.dev.policy.entity.*;
import com.dev.policy.exception.*;
import com.dev.policy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerPolicyService {

    private final CustomerPolicyRepository repo;
    private final BasicPolicyRepository basicPolicyRepo;

    public CustomerPolicyResponse purchase(String email, PurchasePolicyRequest request) {

        // null checks
        if (request.getBasicPolicyId() == null)
            throw new MissingRequiredFieldException("basicPolicyId");
        if (request.getPropertyIdentifier() == null || request.getPropertyIdentifier().isBlank())
            throw new MissingRequiredFieldException("propertyIdentifier");
        if (request.getPremiumAmount() == null)
            throw new MissingRequiredFieldException("premiumAmount");

        BasicPolicy basic = basicPolicyRepo.findById(request.getBasicPolicyId())
                .orElseThrow(() -> new PolicyNotFoundException(request.getBasicPolicyId()));

        if (basic.getStatus() != PolicyStatus.ACTIVE)
            throw new PolicyNotActiveException(basic.getId());

        if (request.getPremiumAmount() < basic.getBasePremium() ||
            request.getPremiumAmount() > basic.getMaxPremium())
            throw new PremiumOutOfRangeException(
                request.getPremiumAmount(), basic.getBasePremium(), basic.getMaxPremium());

        if (repo.existsByPropertyIdentifierAndPolicyType(
                request.getPropertyIdentifier(), basic.getType()))
            throw new DuplicatePropertyInsuranceException(
                request.getPropertyIdentifier(), basic.getType());

        CustomerPolicy cp = CustomerPolicy.builder()
                .customerEmail(email)
                .holderName(request.getHolderName())
                .premiumAmount(request.getPremiumAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .propertyIdentifier(request.getPropertyIdentifier())
                .policyType(basic.getType())
                .status(PurchaseStatus.ACTIVE)
                .basicPolicy(basic)
                .build();

        return toResponse(repo.save(cp));
    }

    public Page<CustomerPolicyResponse> getMyPolicies(String email, Pageable pageable) {
        return repo.findByCustomerEmail(email, pageable).map(this::toResponse);
    }

    public Page<CustomerPolicyResponse> getAll(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResponse);
    }

    public CustomerPolicyResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id)));
    }

    private CustomerPolicyResponse toResponse(CustomerPolicy cp) {
        return CustomerPolicyResponse.builder()
                .id(cp.getId())
                .holderName(cp.getHolderName())
                .customerEmail(cp.getCustomerEmail())
                .premiumAmount(cp.getPremiumAmount())
                .startDate(cp.getStartDate())
                .endDate(cp.getEndDate())
                .propertyIdentifier(cp.getPropertyIdentifier())
                .policyType(cp.getPolicyType())
                .status(cp.getStatus())
                .policyName(cp.getBasicPolicy().getPolicyName())
                .build();
    }

    public Map<String, Long> getCustomerPolicyCounts() {
        return Map.of(
            "sold",       repo.count(),
            "soldActive", repo.countByStatus(PurchaseStatus.ACTIVE),
            "HOME",       repo.countByPolicyType(PolicyType.HOME),
            "VEHICLE",    repo.countByPolicyType(PolicyType.VEHICLE)
        );
    }

    public Map<String, Double> getRevenue() {
        Double total = repo.sumPremiumAmount();
        return Map.of("total", total != null ? total : 0.0);
    }
}