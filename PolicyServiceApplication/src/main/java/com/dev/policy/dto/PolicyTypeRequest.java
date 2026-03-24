package com.dev.policy.dto;

import com.dev.policy.entity.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyTypeRequest {
    @jakarta.validation.constraints.NotBlank(message = "Policy name is mandatory")
    private String name;

    @jakarta.validation.constraints.NotBlank(message = "Description is mandatory")
    private String description;

    @jakarta.validation.constraints.NotNull(message = "Base premium is mandatory")
    @jakarta.validation.constraints.Positive(message = "Base premium must be greater than zero")
    private Double basePremium;

    @jakarta.validation.constraints.NotNull(message = "Coverage amount is mandatory")
    @jakarta.validation.constraints.Positive(message = "Coverage amount must be greater than zero")
    private Double coverageAmount;

    @jakarta.validation.constraints.NotNull(message = "Asset type is mandatory (e.g. VEHICLE or HOME)")
    private AssetType assetType;
}
