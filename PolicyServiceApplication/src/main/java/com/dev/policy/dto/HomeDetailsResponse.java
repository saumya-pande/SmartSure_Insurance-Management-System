package com.dev.policy.dto;

import com.dev.policy.entity.enums.HomeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeDetailsResponse {
    private Long id;
    private Long policyId;
    private HomeType homeType;
    
    // Flat
    private String unitNumber;
    private String societyName;
    
    // Common
    private String propertyAddress;
    private String city;
    private String pincode;
    
    // Villa
    private String propertyId;
    
    // Evaluate
    private Double propertyValue;
    private Double propertySize;
}
