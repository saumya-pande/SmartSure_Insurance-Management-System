package com.dev.claimsservice.dto;


import com.dev.claimsservice.entity.PurchaseStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CustomerPolicyResponse {
    private Long id;
    private String holderName;
    private String customerEmail;
    private Double premiumAmount;
    private Double coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String propertyIdentifier;
    private String policyType;
    private PurchaseStatus status;
    private String policyName;
}