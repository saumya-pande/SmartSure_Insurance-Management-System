package com.dev.policy.dto;

import com.dev.policy.entity.PolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
}