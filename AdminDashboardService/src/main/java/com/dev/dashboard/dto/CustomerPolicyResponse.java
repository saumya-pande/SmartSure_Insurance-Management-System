package com.dev.dashboard.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

import com.dev.dashboard.entity.PolicyType;
import com.dev.dashboard.entity.PurchaseStatus;

@Getter @Setter
public class CustomerPolicyResponse {
    private Long id;
    private String holderName;
    private String customerEmail;
    private Double premiumAmount;
    private Double totalPremium;
    private Double coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String propertyIdentifier;
    private PolicyType policyType;
    private PurchaseStatus status;
    private String policyName;
}