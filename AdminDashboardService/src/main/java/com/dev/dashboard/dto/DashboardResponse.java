package com.dev.dashboard.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardResponse {

    // users
    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;

    // kyc
    private long totalKyc;
    private long pendingKyc;
    private long approvedKyc;
    private long rejectedKyc;

    // basic policies
    private long totalBasicPolicies;
    private long activeBasicPolicies;
    private long inactiveBasicPolicies;
    private Map<String, Long> basicPoliciesByType;

    // purchased policies
    private long totalPoliciesSold;
    private long activePoliciesSold;
    private Map<String, Long> policiesSoldByType;  // HOME / VEHICLE counts
    private double totalRevenue;

    // claims
    private long totalClaims;
    private Map<String, Long> claimsByStatus;      // per ClaimStatus counts
}