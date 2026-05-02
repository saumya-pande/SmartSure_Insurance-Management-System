package com.dev.policy.dto;

import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicPolicyResponse {
    private Long id;
    private String policyName;
    private PolicyType type;
    private Double basePremium;
    private Double maxPremium;
    private String description;
    private Integer maxMonthCoverage;
    private com.dev.policy.entity.BillingCycle billingCycle;
    private PolicyStatus status;
}