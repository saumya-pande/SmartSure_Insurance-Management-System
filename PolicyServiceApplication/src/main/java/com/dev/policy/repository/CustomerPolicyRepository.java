package com.dev.policy.repository;

import com.dev.policy.entity.CustomerPolicy;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.entity.PurchaseStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerPolicyRepository extends JpaRepository<CustomerPolicy, Long> {
    Page<CustomerPolicy> findByCustomerEmail(String email, Pageable pageable);
    boolean existsByPropertyIdentifierAndPolicyType(String propertyIdentifier,
            com.dev.policy.entity.PolicyType policyType);
	    long countByStatus(PurchaseStatus status);
	    long countByPolicyType(PolicyType type);
	    @Query("SELECT SUM(c.premiumAmount) FROM CustomerPolicy c")
	    Double sumPremiumAmount();
}