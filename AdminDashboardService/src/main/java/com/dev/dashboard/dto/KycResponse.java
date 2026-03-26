package com.dev.dashboard.dto;


import com.dev.dashboard.entity.KycStatus;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class KycResponse {
    private Long id;
    private String contactNumber;
    private String documentType;
    private String documentPath;
    private String address;
    private KycStatus status;
    private String userEmail;
}