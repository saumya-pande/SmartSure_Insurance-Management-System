package com.dev.claimsservice.mapper;

import com.dev.claimsservice.dto.ClaimResponse;
import com.dev.claimsservice.dto.ClaimDocumentResponse;
import com.dev.claimsservice.entity.Claim;
import com.dev.claimsservice.entity.ClaimDocument;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
public class ClaimMapper {

    public ClaimResponse toResponse(Claim claim) {
        var documents = claim.getDocuments() == null ? Collections.<ClaimDocument>emptyList() : claim.getDocuments();

        ClaimResponse response = new ClaimResponse();
        response.setId(claim.getId());
        response.setCustomerEmail(claim.getCustomerEmail());
        response.setCustomerPolicyId(claim.getCustomerPolicyId());
        response.setClaimAmount(claim.getClaimAmount());
        response.setStatus(claim.getStatus());
        response.setCreatedAt(claim.getCreatedAt());
        response.setUpdatedAt(claim.getUpdatedAt());
        response.setDocuments(documents.stream()
                .map(this::toDocumentResponse)
                .toList());
        return response;
    }

    private ClaimDocumentResponse toDocumentResponse(ClaimDocument document) {
        return ClaimDocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileUrl("/api/claims/document/" + document.getId())
                .build();
    }
}
