package com.dev.policy.entity;

import com.dev.policy.entity.enums.HomeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "home_details", uniqueConstraints = {
        @UniqueConstraint(name = "home_flat", columnNames = {"unit_number", "society_name", "pincode"}),
        @UniqueConstraint(name = "home_villa", columnNames = {"property_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", nullable = false, unique = true)
    private Policy policy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HomeType homeType;

    // Flat fields
    @Column(name = "unit_number")
    private String unitNumber;

    @Column(name = "society_name")
    private String societyName;

    // Common location fields
    @Column(name = "property_address")
    private String propertyAddress;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String pincode;

    // Villa fields
    @Column(name = "property_id")
    private String propertyId;

    // Common evaluation fields
    @Column(nullable = false)
    private Double propertyValue;

    @Column(nullable = false)
    private Double propertySize;
}
