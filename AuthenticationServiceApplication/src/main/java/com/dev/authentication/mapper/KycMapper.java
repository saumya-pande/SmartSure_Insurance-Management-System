package com.dev.authentication.mapper;

import org.springframework.stereotype.Component;

import com.dev.authentication.dto.KycAdminResponse;
import com.dev.authentication.dto.KycResponse;
import com.dev.authentication.dto.UserResponse;
import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.User;


@Component
public class KycMapper {

    public KycResponse toResponse(Kyc kyc) {
        return KycResponse.builder()
                .id(kyc.getId())
                .contactNumber(kyc.getContactNumber())
                .documentType(kyc.getDocumentType())
                .address(kyc.getAddress())
                .status(kyc.getStatus())
                .userEmail(kyc.getUser().getEmail())
                .fileUrl(kyc.getDocumentPath() != null
                        ? "/api/kyc/" + kyc.getId() + "/file"
                        : null)
                .build();
    }

    public KycAdminResponse toAdminResponse(Kyc kyc) {
        return KycAdminResponse.builder()
                .id(kyc.getId())
                .contactNumber(kyc.getContactNumber())
                .documentType(kyc.getDocumentType())
                .documentPath(kyc.getDocumentPath())
                .address(kyc.getAddress())
                .status(kyc.getStatus())
                .userEmail(kyc.getUser().getEmail())
                .fileUrl(kyc.getDocumentPath() != null
                        ? "/api/kyc/" + kyc.getId() + "/file"
                        : null)
                .build();
    }

    public UserResponse toUserResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setEmail(user.getEmail());
        r.setRole(user.getRole().name());
        r.setActive(user.isActive());
        return r;
    }
}
