package com.dev.policy.repository;

import com.dev.policy.entity.VehicleDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, Long> {
    boolean existsByVehicleNumber(String vehicleNumber);
}
