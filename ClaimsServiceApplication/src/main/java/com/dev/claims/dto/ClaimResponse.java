package com.dev.claims.dto;

import com.dev.claims.entity.enums.ClaimStatus;
import com.dev.claims.entity.enums.IncidentType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ClaimResponse {
    private Long id;
    private Long policyId;
    private Long userId;
    private String userEmail;
    private IncidentType incidentType;
    private LocalDate incidentDate;
    private String description;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String adminRemarks;
}
