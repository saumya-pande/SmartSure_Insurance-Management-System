package com.dev.dashboard.service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dev.dashboard.clients.AuthClient;
import com.dev.dashboard.clients.ClaimsClient;
import com.dev.dashboard.clients.PolicyClient;
import com.dev.dashboard.dto.BasicPolicyRequest;
import com.dev.dashboard.dto.BasicPolicyResponse;
import com.dev.dashboard.dto.ClaimResponse;
import com.dev.dashboard.dto.CustomerPolicyResponse;
import com.dev.dashboard.dto.DashboardResponse;
import com.dev.dashboard.dto.KycResponse;
import com.dev.dashboard.dto.UserResponse;
import com.dev.dashboard.entity.ClaimStatus;
import com.dev.dashboard.entity.KycStatus;

import com.dev.dashboard.entity.PurchaseStatus;
import com.dev.dashboard.entity.Role;
import com.dev.dashboard.entity.PolicyType;
import com.dev.dashboard.entity.PolicyStatus;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AuthClient authClient;
    private final PolicyClient policyClient;
    private final ClaimsClient claimsClient;

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    /** Extract the admin's email from the SecurityContext (set by HeaderAuthFilter). */
    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // ── Dashboard ─────────────────────────────────────────────

    public DashboardResponse getDashboard() {
        String email = currentEmail();
        Map<String, Long> userCounts   = authClient.getUserCounts(ADMIN_ROLE, email);
        Map<String, Long> kycCounts    = authClient.getKycCounts(ADMIN_ROLE, email);
        Map<String, Long> policyCounts = policyClient.getPolicyCounts(ADMIN_ROLE, email);
        Map<String, Double> policyRevenue = policyClient.getRevenue(ADMIN_ROLE, email);
        Map<String, Long> claimCounts  = claimsClient.getClaimCounts(ADMIN_ROLE, email);
        Map<String, Double> claimPayouts = claimsClient.getPayouts(ADMIN_ROLE, email);

        Double grossRevenue = policyRevenue.getOrDefault("total", 0.0);
        Double totalPayouts = claimPayouts.getOrDefault("total", 0.0);
        Double netRevenue = grossRevenue - totalPayouts;

        return DashboardResponse.builder()
                // users
                .totalUsers(userCounts.getOrDefault("total", 0L))
                .activeUsers(userCounts.getOrDefault("active", 0L))
                .suspendedUsers(userCounts.getOrDefault("suspended", 0L))
                // kyc
                .totalKyc(kycCounts.getOrDefault("total", 0L))
                .pendingKyc(kycCounts.getOrDefault("PENDING", 0L))
                .approvedKyc(kycCounts.getOrDefault("APPROVED", 0L))
                .rejectedKyc(kycCounts.getOrDefault("REJECTED", 0L))
                // basic policies
                .totalBasicPolicies(policyCounts.getOrDefault("total", 0L))
                .activeBasicPolicies(policyCounts.getOrDefault("ACTIVE", 0L))
                .inactiveBasicPolicies(policyCounts.getOrDefault("INACTIVE", 0L))
                .basicPoliciesByType(Map.of(
                        "HOME", policyCounts.getOrDefault("basicHOME", 0L),
                        "VEHICLE", policyCounts.getOrDefault("basicVEHICLE", 0L)
                ))
                // purchased policies
                .totalPoliciesSold(policyCounts.getOrDefault("sold", 0L))
                .activePoliciesSold(policyCounts.getOrDefault("soldActive", 0L))
                .policiesSoldByType(Map.of(
                        "HOME", policyCounts.getOrDefault("soldHOME", 0L),
                        "VEHICLE", policyCounts.getOrDefault("soldVEHICLE", 0L)
                ))
                .grossRevenue(grossRevenue)
                .totalPayouts(totalPayouts)
                .totalRevenue(netRevenue)
                // claims
                .totalClaims(claimCounts.getOrDefault("total", 0L))
                .claimsByStatus(Map.of(
                        "DRAFT",        claimCounts.getOrDefault("DRAFT", 0L),
                        "SUBMITTED",    claimCounts.getOrDefault("SUBMITTED", 0L),
                        "UNDER_REVIEW", claimCounts.getOrDefault("UNDER_REVIEW", 0L),
                        "APPROVED",     claimCounts.getOrDefault("APPROVED", 0L),
                        "REJECTED",     claimCounts.getOrDefault("REJECTED", 0L),
                        "CLOSED",       claimCounts.getOrDefault("CLOSED", 0L)
                ))
                .build();
    }

    // ── Users ─────────────────────────────────────────────────

    public Page<UserResponse> getUsers(String email, String name,
            Role role, Boolean active, int page, int size) {
        return authClient.getUsers(email, name, role, active, page, size, ADMIN_ROLE, currentEmail());
    }

    public UserResponse toggleUserStatus(Long id, boolean active) {
        return authClient.toggleUserStatus(id, active, ADMIN_ROLE, currentEmail());
    }

    // ── KYC ───────────────────────────────────────────────────

    public Page<KycResponse> getKyc(KycStatus status, String email,
            int page, int size) {
        return authClient.getKyc(status, email, page, size, ADMIN_ROLE, currentEmail());
    }

    public KycResponse updateKycStatus(Long id, KycStatus status) {
        return authClient.updateKycStatus(id, status, ADMIN_ROLE, currentEmail());
    }

    // ── Basic Policies ────────────────────────────────────────

    public BasicPolicyResponse createPolicy(BasicPolicyRequest request) {
        return policyClient.createPolicy(request, ADMIN_ROLE, currentEmail());
    }

    public BasicPolicyResponse updatePolicy(Long id, BasicPolicyRequest request) {
        return policyClient.updatePolicy(id, request, ADMIN_ROLE, currentEmail());
    }

    public void deletePolicy(Long id) {
        policyClient.deletePolicy(id, ADMIN_ROLE, currentEmail());
    }

    public BasicPolicyResponse updatePolicyStatus(Long id, PolicyStatus status) {
        return policyClient.updatePolicyStatus(id, status, ADMIN_ROLE, currentEmail());
    }

    public Page<BasicPolicyResponse> getBasicPolicies(PolicyType type,
            PolicyStatus status, String policyName,
            int page, int size, String sortBy) {
        return policyClient.getBasicPolicies(
                type, status, policyName, page, size, sortBy, ADMIN_ROLE, currentEmail());
    }

    // ── Customer Policies ─────────────────────────────────────

    public Page<CustomerPolicyResponse> getCustomerPolicies(String email,
            PolicyType policyType, PurchaseStatus status,
            Double minPremium, Double maxPremium,
            String startDate, String endDate,
            int page, int size, String sortBy) {
        return policyClient.getCustomerPolicies(
                email, policyType, status, minPremium, maxPremium,
                startDate, endDate, page, size, sortBy, ADMIN_ROLE, currentEmail());
    }

    // ── Claims ────────────────────────────────────────────────

    public Page<ClaimResponse> getClaims(ClaimStatus status, String email,
            String startDate, String endDate, int page, int size) {
        return claimsClient.getClaims(
                status, email, startDate, endDate, page, size, ADMIN_ROLE, currentEmail());
    }

    public ClaimResponse overrideClaimStatus(Long id, ClaimStatus status) {
        return claimsClient.overrideClaimStatus(id, status, ADMIN_ROLE, currentEmail());
    }
}
