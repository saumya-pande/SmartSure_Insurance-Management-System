package com.dev.dashboard.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

import com.dev.dashboard.entity.ClaimStatus;

@Getter @Setter
public class ClaimResponse {
    private Long id;
    private String customerEmail;
    private Long customerPolicyId;
    private Double claimAmount;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> documentPaths;
}