package com.dev.policy.mapper;

import com.dev.policy.dto.VehicleDetailsRequest;
import com.dev.policy.dto.VehicleDetailsResponse;
import com.dev.policy.entity.VehicleDetails;
import org.springframework.stereotype.Component;

@Component
public class VehicleDetailsMapper {

    public VehicleDetailsResponse toResponse(VehicleDetails entity) {
        if (entity == null) return null;
        return VehicleDetailsResponse.builder()
                .id(entity.getId())
                .policyId(entity.getPolicy() != null ? entity.getPolicy().getId() : null)
                .vehicleNumber(entity.getVehicleNumber())
                .model(entity.getModel())
                .year(entity.getYear())
                .build();
    }

    public VehicleDetails toEntity(VehicleDetailsRequest request) {
        if (request == null) return null;
        return VehicleDetails.builder()
                .vehicleNumber(request.getVehicleNumber())
                .model(request.getModel())
                .year(request.getYear())
                .build();
    }
}
