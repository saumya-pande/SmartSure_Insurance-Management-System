package com.dev.policy.repository;

import com.dev.policy.entity.CustomerPolicy;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.entity.PurchaseStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface CustomerPolicyRepository extends JpaRepository<CustomerPolicy, Long> {
    Page<CustomerPolicy> findByCustomerEmail(String email, Pageable pageable);
    boolean existsByPropertyIdentifierAndPolicyType(String propertyIdentifier,
            PolicyType policyType);
    long countByStatus(PurchaseStatus status);
    long countByPolicyType(PolicyType type);

    @Query("SELECT SUM(c.totalPremium) FROM CustomerPolicy c")
    Double sumTotalPremium();

    /**
     * Flexible filter query for admin — all filters are optional.
     * Uses COALESCE / null-check pattern so each param is ignored when null.
     */
    @Query("""
            SELECT cp FROM CustomerPolicy cp
            WHERE (:email IS NULL OR cp.customerEmail LIKE %:email%)
              AND (:policyType IS NULL OR cp.policyType = :policyType)
              AND (:status IS NULL OR cp.status = :status)
              AND (:minPremium IS NULL OR cp.premiumAmount >= :minPremium)
              AND (:maxPremium IS NULL OR cp.premiumAmount <= :maxPremium)
              AND (:startDate IS NULL OR cp.startDate >= :startDate)
              AND (:endDate IS NULL OR cp.endDate <= :endDate)
            """)
    Page<CustomerPolicy> findByFilters(
            @Param("email") String email,
            @Param("policyType") PolicyType policyType,
            @Param("status") PurchaseStatus status,
            @Param("minPremium") Double minPremium,
            @Param("maxPremium") Double maxPremium,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}