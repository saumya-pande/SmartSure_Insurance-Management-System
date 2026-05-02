package com.dev.policy.dto;

import com.dev.policy.entity.PolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BasicPolicyRequest {

    @NotBlank(message = "Policy name is required")
    private String policyName;

    @NotNull(message = "Policy type is required (HOME or VEHICLE)")
    private PolicyType type;

    @NotNull(message = "Base premium is required")
    @Positive(message = "Base premium must be a positive number")
    private Double basePremium;

    @NotNull(message = "Max premium is required")
    @Positive(message = "Max premium must be a positive number")
    private Double maxPremium;
 
    @NotNull(message = "Min coverage amount is required")
    @Positive(message = "Min coverage amount must be a positive number")
    private Double minCoverageAmount;
 
    @NotNull(message = "Max coverage amount is required")
    @Positive(message = "Max coverage amount must be a positive number")
    private Double maxCoverageAmount;

    @Size(max = 250, message = "Description must be at most 250 characters")
    private String description;

    @NotNull(message = "Max month coverage is required")
    @Positive(message = "Max month coverage must be a positive number")
    private Integer maxMonthCoverage;

    @NotNull(message = "Billing cycle is required")
    private com.dev.policy.entity.BillingCycle billingCycle;
}