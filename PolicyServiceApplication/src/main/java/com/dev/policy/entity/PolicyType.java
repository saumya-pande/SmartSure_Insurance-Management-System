package com.dev.policy.entity;

import com.dev.policy.entity.enums.AssetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "policy_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double basePremium;

    @Column(nullable = false)
    private Double coverageAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType; // VEHICLE or HOME

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
