package com.dev.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDetailsRequest {
    private String vehicleNumber;
    private String model;
    private Integer year;
}
