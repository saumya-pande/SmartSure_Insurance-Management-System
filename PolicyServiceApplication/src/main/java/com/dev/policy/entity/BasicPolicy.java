package com.dev.policy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BasicPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String policyName;

    @Enumerated(EnumType.STRING)
    private PolicyType type;

    private Double basePremium;
    private Double maxPremium;
 
    private Double minCoverageAmount;
    private Double maxCoverageAmount;

    @Column(length = 250)
    private String description;

    private Integer maxMonthCoverage;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PolicyStatus status = PolicyStatus.ACTIVE;
}