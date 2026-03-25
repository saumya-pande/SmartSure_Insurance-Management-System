package com.dev.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.authentication.entity.Kyc;

public interface KycRepository extends JpaRepository<Kyc, Long> {}
