package com.dev.policy.mapper;

import com.dev.policy.dto.PolicyResponse;
import com.dev.policy.entity.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {

    @Autowired
    private PolicyTypeMapper policyTypeMapper;

    public PolicyResponse toResponse(Policy entity) {
        if (entity == null) return null;
        return PolicyResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .policyType(policyTypeMapper.toResponse(entity.getPolicyType()))
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
