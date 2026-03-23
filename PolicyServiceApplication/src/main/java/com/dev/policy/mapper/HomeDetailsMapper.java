package com.dev.policy.mapper;

import com.dev.policy.dto.HomeDetailsRequest;
import com.dev.policy.dto.HomeDetailsResponse;
import com.dev.policy.entity.HomeDetails;
import org.springframework.stereotype.Component;

@Component
public class HomeDetailsMapper {

    public HomeDetailsResponse toResponse(HomeDetails entity) {
        if (entity == null) return null;
        return HomeDetailsResponse.builder()
                .id(entity.getId())
                .policyId(entity.getPolicy() != null ? entity.getPolicy().getId() : null)
                .homeType(entity.getHomeType())
                .unitNumber(entity.getUnitNumber())
                .societyName(entity.getSocietyName())
                .propertyAddress(entity.getPropertyAddress())
                .city(entity.getCity())
                .pincode(entity.getPincode())
                .propertyId(entity.getPropertyId())
                .propertyValue(entity.getPropertyValue())
                .propertySize(entity.getPropertySize())
                .build();
    }

    public HomeDetails toEntity(HomeDetailsRequest request) {
        if (request == null) return null;
        return HomeDetails.builder()
                .homeType(request.getHomeType())
                .unitNumber(request.getUnitNumber())
                .societyName(request.getSocietyName())
                .propertyAddress(request.getPropertyAddress())
                .city(request.getCity())
                .pincode(request.getPincode())
                .propertyId(request.getPropertyId())
                .propertyValue(request.getPropertyValue())
                .propertySize(request.getPropertySize())
                .build();
    }
}
