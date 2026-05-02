package com.dev.policy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CustomerPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;
    private String holderName;

    private Double premiumAmount; // rate per billing cycle
    private Double totalPremium;   // total contract value
    private Double coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;

    // HOME: flat/house number, VEHICLE: vehicle number
    private String propertyIdentifier;

    @Enumerated(EnumType.STRING)
    private PolicyType policyType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PurchaseStatus status = PurchaseStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "basic_policy_id")
    private BasicPolicy basicPolicy;
}