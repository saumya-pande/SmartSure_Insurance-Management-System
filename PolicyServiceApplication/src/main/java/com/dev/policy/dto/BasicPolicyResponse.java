package com.dev.policy.dto;


import com.dev.policy.entity.PolicyType;
import lombok.Getter;
import lombok.Setter;

import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class BasicPolicyResponse {
    private Long id;
    private String policyName;
    private PolicyType type;
    private Double basePremium;
    private Double maxPremium;
    private PolicyStatus status;
}