package com.dev.claimsservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClaimRequest {

    @NotNull(message = "Customer policy ID is required")
    private Long customerPolicyId;

    @NotNull(message = "Claim amount is required")
    @Positive(message = "Claim amount must be a positive number")
    private Double claimAmount;
}