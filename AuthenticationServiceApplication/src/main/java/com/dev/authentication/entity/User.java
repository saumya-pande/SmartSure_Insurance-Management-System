package com.dev.authentication.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    private String password;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;
    
    @Builder.Default
    private boolean active = true;
    

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Kyc kyc;
}