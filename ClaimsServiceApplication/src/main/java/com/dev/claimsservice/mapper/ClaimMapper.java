package com.dev.claimsservice.mapper;

import com.dev.claimsservice.dto.ClaimResponse;
import com.dev.claimsservice.entity.Claim;
import com.dev.claimsservice.entity.ClaimDocument;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {

    public ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .customerEmail(claim.getCustomerEmail())
                .customerPolicyId(claim.getCustomerPolicyId())
                .claimAmount(claim.getClaimAmount())
                .status(claim.getStatus())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .documentPaths(claim.getDocuments().stream()
                        .map(ClaimDocument::getFilePath)
                        .toList())
                .build();
    }
}
