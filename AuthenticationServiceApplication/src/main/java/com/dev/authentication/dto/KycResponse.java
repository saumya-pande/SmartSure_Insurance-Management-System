package com.dev.authentication.dto;

import com.dev.authentication.entity.KycStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class KycResponse {
    private Long id;
    private String contactNumber;
    private String documentType;
    private String address;
    private KycStatus status;
    private String userEmail;
}