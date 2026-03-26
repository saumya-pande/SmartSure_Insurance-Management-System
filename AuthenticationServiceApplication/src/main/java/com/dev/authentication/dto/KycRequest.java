package com.dev.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class KycRequest {

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be a 10-digit number")
    private String contactNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Document type is required")
    private String documentType;
}