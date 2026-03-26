package com.dev.policy.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class PurchasePolicyRequest {

    @NotNull(message = "Basic policy ID is required")
    private Long basicPolicyId;

    @NotBlank(message = "Holder name is required")
    private String holderName;

    @NotNull(message = "Premium amount is required")
    @Positive(message = "Premium amount must be a positive number")
    private Double premiumAmount;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotBlank(message = "Property identifier is required (flat/house number or vehicle number)")
    private String propertyIdentifier;
}