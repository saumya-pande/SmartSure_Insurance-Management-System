package com.dev.policy.client;

import lombok.Data;

@Data
public class KycResponse {
    private Long id;
    private String contactNumber;
    private String documentType;
    private String address;
    private String status;
    private String userEmail;
}
