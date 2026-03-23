package com.dev.policy.mapper;

import com.dev.policy.dto.PremiumResponse;
import com.dev.policy.entity.Premium;
import org.springframework.stereotype.Component;

@Component
public class PremiumMapper {

    public PremiumResponse toResponse(Premium entity) {
        if (entity == null) return null;
        return PremiumResponse.builder()
                .id(entity.getId())
                .policyId(entity.getPolicy() != null ? entity.getPolicy().getId() : null)
                .amount(entity.getAmount())
                .dueDate(entity.getDueDate())
                .paidDate(entity.getPaidDate())
                .paymentStatus(entity.getPaymentStatus())
                .build();
    }
}
