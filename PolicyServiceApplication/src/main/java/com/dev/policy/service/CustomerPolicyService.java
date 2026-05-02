package com.dev.policy.service;

import com.dev.policy.client.KycClient;
import com.dev.policy.client.KycResponse;
import com.dev.policy.config.RabbitMQConfig;
import com.dev.policy.dto.*;
import com.dev.policy.entity.*;
import com.dev.policy.exception.*;
import com.dev.policy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
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

        // ── duration validation ───────────────────────────────────
        if (basic.getMaxMonthCoverage() != null
                && request.getStartDate() != null
                && request.getEndDate() != null) {
            long months = java.time.Period.between(request.getStartDate(), request.getEndDate()).toTotalMonths();
            if (months > basic.getMaxMonthCoverage()) {
                throw new InvalidFieldValueException("endDate", request.getEndDate(),
                        "Coverage duration (" + months + " months) exceeds the maximum allowed "
                        + basic.getMaxMonthCoverage() + " months for this policy.");
            }
        }
        
        long months = java.time.Period.between(request.getStartDate(), request.getEndDate()).toTotalMonths();
        if (months <= 0) months = 1; // minimum 1 month/unit
        
        Double totalAmount = request.getPremiumAmount();
        if (basic.getBillingCycle() == BillingCycle.MONTHLY) {
            totalAmount = request.getPremiumAmount() * months;
        } else if (basic.getBillingCycle() == BillingCycle.YEARLY) {
            long years = months / 12;
            if (years == 0) years = 1; // minimum 1 year charge if yearly
            totalAmount = request.getPremiumAmount() * years;
        }
 
        Double coverageAmount = basic.getMinCoverageAmount();
        if (basic.getMaxPremium() != null && basic.getBasePremium() != null && basic.getMaxPremium() > basic.getBasePremium()) {
            Double ratio = (request.getPremiumAmount() - basic.getBasePremium()) / (basic.getMaxPremium() - basic.getBasePremium());
            coverageAmount = basic.getMinCoverageAmount() + ratio * (basic.getMaxCoverageAmount() - basic.getMinCoverageAmount());
        }

        CustomerPolicy cp = CustomerPolicy.builder()
                .customerEmail(email)
                .holderName(request.getHolderName())
                .premiumAmount(request.getPremiumAmount())
                .totalPremium(totalAmount)
                .coverageAmount(coverageAmount)
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
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_PURCHASED,
                    payload);
        } catch (Exception e) {
            log.error("Failed to send policy purchase event to RabbitMQ for policy ID: {}. Error: {}", saved.getId(), e.getMessage());
        }

        return toResponse(saved);
    }

    public Page<CustomerPolicyResponse> getMyPolicies(String email, Pageable pageable) {
        Page<CustomerPolicy> page = repo.findByCustomerEmail(email, pageable);
        if (page.isEmpty()) {
            throw new PolicyNotFoundException("No policies purchased");
        }
        return page.map(this::toResponse);
    }

    /** Simple getAll — no filters (used by customer-facing controller). */
    public Page<CustomerPolicyResponse> getAll(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResponse);
    }

    /** Admin getAll with full filter support. */
    public Page<CustomerPolicyResponse> getAll(String email, PolicyType policyType,
            PurchaseStatus status, Double minPremium, Double maxPremium,
            String startDateStr, String endDateStr, Pageable pageable) {

        LocalDate startDate = (startDateStr != null && !startDateStr.isBlank())
                ? LocalDate.parse(startDateStr) : null;
        LocalDate endDate = (endDateStr != null && !endDateStr.isBlank())
                ? LocalDate.parse(endDateStr) : null;

        // If no filters provided, use simple findAll
        if (email == null && policyType == null && status == null
                && minPremium == null && maxPremium == null
                && startDate == null && endDate == null) {
            return repo.findAll(pageable).map(this::toResponse);
        }

        return repo.findByFilters(email, policyType, status,
                minPremium, maxPremium, startDate, endDate, pageable)
                .map(this::toResponse);
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
                .totalPremium(cp.getTotalPremium())
                .coverageAmount(cp.getCoverageAmount())
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
        Double total = repo.sumTotalPremium();
        return Map.of("total", total != null ? total : 0.0);
    }
}