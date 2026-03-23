package com.dev.policy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicle_details", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehicle_number", columnNames = {"vehicle_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", nullable = false, unique = true)
    private Policy policy;

    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private Integer year;
}
