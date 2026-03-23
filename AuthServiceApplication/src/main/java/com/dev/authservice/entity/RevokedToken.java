package com.dev.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity to store revoked JWT tokens for logout functionality.
 */
@Entity
@Table(name = "revoked_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @Column(name = "token", length = 512) // Token string as primary key
    private String token;
}
