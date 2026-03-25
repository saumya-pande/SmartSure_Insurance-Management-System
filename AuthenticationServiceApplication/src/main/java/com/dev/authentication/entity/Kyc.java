package com.dev.authentication.entity;

import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String contactNumber;
    
    private String documentType;

    @Value("${file.upload-dir}")
    private String documentPath;  

    private String address;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private KycStatus status = KycStatus.REJECTED;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}