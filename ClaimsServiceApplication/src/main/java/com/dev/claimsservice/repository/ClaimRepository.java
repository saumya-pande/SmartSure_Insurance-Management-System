package com.dev.claimsservice.repository;


import com.dev.claimsservice.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Page<Claim> findByCustomerEmail(String email, Pageable pageable);
    Page<Claim> findByStatus(com.dev.claimsservice.entity.ClaimStatus status, Pageable pageable);
    boolean existsByCustomerPolicyIdAndStatusNot(Long customerPolicyId, com.dev.claimsservice.entity.ClaimStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(c.claimAmount) FROM Claim c WHERE c.status = :status")
    Double sumClaimAmountByStatus(@org.springframework.data.repository.query.Param("status") com.dev.claimsservice.entity.ClaimStatus status);

    long countByStatus(com.dev.claimsservice.entity.ClaimStatus status);
}