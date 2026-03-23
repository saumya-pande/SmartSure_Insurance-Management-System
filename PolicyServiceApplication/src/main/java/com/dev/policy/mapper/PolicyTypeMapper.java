package com.dev.policy.mapper;

import com.dev.policy.dto.PolicyTypeRequest;
import com.dev.policy.dto.PolicyTypeResponse;
import com.dev.policy.entity.PolicyType;
import org.springframework.stereotype.Component;

@Component
public class PolicyTypeMapper {

    public PolicyTypeResponse toResponse(PolicyType entity) {
        if (entity == null) return null;
        return PolicyTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .basePremium(entity.getBasePremium())
                .coverageAmount(entity.getCoverageAmount())
                .assetType(entity.getAssetType())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PolicyType toEntity(PolicyTypeRequest request) {
        if (request == null) return null;
        return PolicyType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePremium(request.getBasePremium())
                .coverageAmount(request.getCoverageAmount())
                .assetType(request.getAssetType())
                .build();
    }
}
