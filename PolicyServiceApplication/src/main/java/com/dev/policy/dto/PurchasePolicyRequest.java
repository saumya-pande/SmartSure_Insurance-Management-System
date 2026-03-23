package com.dev.policy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasePolicyRequest {
    
    @NotNull(message = "Policy Type ID is required")
    private Long policyTypeId;
    
    // The user will send either homeDetails or vehicleDetails depending on the product
    private HomeDetailsRequest homeDetails;
    private VehicleDetailsRequest vehicleDetails;
}
