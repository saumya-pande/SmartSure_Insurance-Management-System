package com.dev.claimsservice.dto;


import com.dev.claimsservice.entity.ClaimStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private String customerEmail;
    private Long customerPolicyId;
    private Double claimAmount;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ClaimDocumentResponse> documents;
}
