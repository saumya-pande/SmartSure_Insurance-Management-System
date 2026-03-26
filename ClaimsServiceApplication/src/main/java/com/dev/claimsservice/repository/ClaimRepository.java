package com.dev.claimsservice.repository;


import com.dev.claimsservice.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Page<Claim> findByCustomerEmail(String email, Pageable pageable);
}