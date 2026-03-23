package com.dev.authservice.repository;

import com.dev.authservice.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing revoked JWT tokens.
 */
@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
}
