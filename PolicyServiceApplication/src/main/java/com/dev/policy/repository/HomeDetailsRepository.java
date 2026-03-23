package com.dev.policy.repository;

import com.dev.policy.entity.HomeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeDetailsRepository extends JpaRepository<HomeDetails, Long> {
    
    // Constraint 1: Flat uniqueness
    boolean existsByUnitNumberAndSocietyNameAndPincode(String unitNumber, String societyName, String pincode);
    
    // Constraint 2: Villa uniqueness
    boolean existsByPropertyId(String propertyId);
}
