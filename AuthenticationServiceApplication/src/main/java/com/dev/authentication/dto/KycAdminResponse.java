package com.dev.authentication.dto;

import com.dev.authentication.entity.KycStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KycAdminResponse {
    private Long id;
    private String contactNumber;
    private String documentType;
    private String documentPath;
    private String address;
    private KycStatus status;
    private String userEmail;
    private String fileUrl;
}