package com.dev.claimsservice.repository;


import com.dev.claimsservice.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Page<Claim> findByCustomerEmail(String email, Pageable pageable);
    
    Page<Claim> findByStatus(com.dev.claimsservice.entity.ClaimStatus status, Pageable pageable);
    
    Page<Claim> findByStatusAndCustomerEmailContaining(com.dev.claimsservice.entity.ClaimStatus status, String email, Pageable pageable);
    
    Page<Claim> findByCustomerEmailContaining(String email, Pageable pageable);
    
    boolean existsByCustomerPolicyIdAndStatusNot(Long customerPolicyId, com.dev.claimsservice.entity.ClaimStatus status);

    @Query("SELECT SUM(c.claimAmount) FROM Claim c WHERE c.status = :status")
    Double sumClaimAmountByStatus(@Param("status") com.dev.claimsservice.entity.ClaimStatus status);

    long countByStatus(com.dev.claimsservice.entity.ClaimStatus status);

    /**
     * Flexible filter query for admin — all filters are optional.
     */
    @Query("""
            SELECT c FROM Claim c
            WHERE (:status IS NULL OR c.status = :status)
              AND (:email IS NULL OR c.customerEmail LIKE %:email%)
              AND (CAST(:startDate AS timestamp) IS NULL OR c.createdAt >= :startDate)
              AND (CAST(:endDate AS timestamp) IS NULL OR c.createdAt <= :endDate)
            """)
    Page<Claim> findByFilters(
            @Param("status") com.dev.claimsservice.entity.ClaimStatus status,
            @Param("email") String email,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}