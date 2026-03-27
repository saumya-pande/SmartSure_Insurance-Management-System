package com.dev.policy.service;

import com.dev.policy.client.KycClient;
import com.dev.policy.client.KycResponse;
import com.dev.policy.config.RabbitMQConfig;
import com.dev.policy.dto.*;
import com.dev.policy.entity.*;
import com.dev.policy.exception.*;
import com.dev.policy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerPolicyService {

    private final CustomerPolicyRepository repo;
    private final BasicPolicyRepository basicPolicyRepo;
    private final KycClient kycClient;
    private final RabbitTemplate rabbitTemplate;

    public CustomerPolicyResponse purchase(String email, String role, PurchasePolicyRequest request) {

        // ── KYC verification ──────────────────────────────────────
        try {
            KycResponse kyc = kycClient.getMyKyc(email, role);
            if (!"APPROVED".equalsIgnoreCase(kyc.getStatus())) {
                throw new InvalidOperationException(
                        "KYC is not approved. Current status: " + kyc.getStatus() +
                        ". Please wait for admin approval before purchasing a policy.");
            }
        } catch (feign.FeignException.NotFound e) {
            throw new InvalidOperationException(
                    "No KYC found for your account. Please upload your KYC documents first.");
        } catch (feign.FeignException.Forbidden e) {
            throw new InvalidOperationException(
                    "Access Denied during KYC check. Ensure you are logged in as a CUSTOMER.");
        }

        // ── field validation ──────────────────────────────────────
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

        CustomerPolicy saved = repo.save(cp);

        // ── publish purchase event for email notification ─────────
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("policyId", saved.getId());
        payload.put("policyName", basic.getPolicyName());
        payload.put("amount", saved.getPremiumAmount());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_PURCHASED,
                payload);

        return toResponse(saved);
    }

    public Page<CustomerPolicyResponse> getMyPolicies(String email, Pageable pageable) {
        Page<CustomerPolicy> page = repo.findByCustomerEmail(email, pageable);
        if (page.isEmpty()) {
            throw new PolicyNotFoundException("No policies purchased");
        }
        return page.map(this::toResponse);
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