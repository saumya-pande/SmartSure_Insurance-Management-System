package com.dev.authentication.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    @Email
    @Column(unique = true)
    private String email;
    private String password;
}