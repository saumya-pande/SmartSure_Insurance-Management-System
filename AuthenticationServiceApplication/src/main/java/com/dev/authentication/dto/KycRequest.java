package com.dev.authentication.dto;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycRequest {
    private String contactNumber;
    private String address;
    private String documentType;
}