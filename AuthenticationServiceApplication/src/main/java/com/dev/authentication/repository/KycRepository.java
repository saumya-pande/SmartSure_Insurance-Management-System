package com.dev.authentication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.KycStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KycRepository extends JpaRepository<Kyc, Long> {
    Optional<Kyc> findByUserId(Long userId);

    long countByStatus(KycStatus status);

    Page<Kyc> findByStatusAndUserEmailContaining(KycStatus status, String email, Pageable pageable);

    Page<Kyc> findByStatus(KycStatus status, Pageable pageable);

    Page<Kyc> findByUserEmailContaining(String email, Pageable pageable);
}
