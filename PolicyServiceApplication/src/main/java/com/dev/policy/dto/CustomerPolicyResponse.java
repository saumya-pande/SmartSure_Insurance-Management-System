package com.dev.policy.dto;


import com.dev.policy.entity.PolicyType;
import com.dev.policy.entity.PurchaseStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter @Builder
public class CustomerPolicyResponse {
    private Long id;
    private String holderName;
    private String customerEmail;
    private Double premiumAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String propertyIdentifier;
    private PolicyType policyType;
    private PurchaseStatus status;
    private String policyName;
}