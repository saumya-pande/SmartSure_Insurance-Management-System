package com.dev.authentication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.KycStatus;

public interface KycRepository extends JpaRepository<Kyc, Long> {
        Optional<Kyc> findByUserId(Long userId);

        long countByStatus(KycStatus status);
}
