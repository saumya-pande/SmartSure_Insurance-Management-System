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
    private String name;
    private String description;
    private Double basePremium;
    private Double coverageAmount;
    private AssetType assetType;
}
