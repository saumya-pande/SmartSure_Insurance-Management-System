package com.dev.dashboard.dto;

import com.dev.dashboard.entity.PolicyStatus;
import com.dev.dashboard.entity.PolicyType;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BasicPolicyResponse {
    private Long id;
    private String policyName;
    private PolicyType type;
    private Double basePremium;
    private Double maxPremium;
    private String description;
    private Integer maxMonthCoverage;
    private com.dev.dashboard.entity.BillingCycle billingCycle;
    private PolicyStatus status;
}