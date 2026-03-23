package com.dev.policy.dto;

import com.dev.policy.entity.enums.PolicyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyResponse {
    private Long id;
    private Long userId; 
    private PolicyTypeResponse policyType;
    private PolicyStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    
    private HomeDetailsResponse homeDetails;
    private VehicleDetailsResponse vehicleDetails;
}
