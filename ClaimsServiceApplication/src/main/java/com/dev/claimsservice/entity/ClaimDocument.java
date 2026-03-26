package com.dev.claimsservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String filePath;
    private String fileType;  // pdf, jpeg, png

    @ManyToOne
    @JoinColumn(name = "claim_id")
    private Claim claim;
}
