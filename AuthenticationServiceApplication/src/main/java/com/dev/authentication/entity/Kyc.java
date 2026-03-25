package com.dev.authentication.entity;

import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.*;
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

    private String documentType;

    @Value("${file.upload-dir}")
    private String documentPath;  

    private String address;
    
    @Enumerated(EnumType.STRING)
    private KycStatus status;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}