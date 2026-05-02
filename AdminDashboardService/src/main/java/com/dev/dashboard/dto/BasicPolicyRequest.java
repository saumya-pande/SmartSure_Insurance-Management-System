package com.dev.dashboard.dto;

import com.dev.dashboard.entity.PolicyType;
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

    @Size(max = 250, message = "Description must be at most 250 characters")
    private String description;

    @NotNull(message = "Max month coverage is required")
    @Positive(message = "Max month coverage must be a positive number")
    private Integer maxMonthCoverage;

    @NotNull(message = "Billing cycle is required")
    private com.dev.dashboard.entity.BillingCycle billingCycle;
}