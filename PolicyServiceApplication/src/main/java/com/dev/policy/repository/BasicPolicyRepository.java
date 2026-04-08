package com.dev.policy.repository;


import com.dev.policy.entity.BasicPolicy;
import com.dev.policy.entity.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dev.policy.entity.PolicyType;

public interface BasicPolicyRepository extends JpaRepository<BasicPolicy, Long> {
    Page<BasicPolicy> findByStatus(PolicyStatus status, Pageable pageable);
    Page<BasicPolicy> findByType(PolicyType type, Pageable pageable);
    Page<BasicPolicy> findByStatusAndType(PolicyStatus status, PolicyType type, Pageable pageable);
    long countByStatus(PolicyStatus status);
    long countByType(PolicyType type);

    // policyName filter combinations (case-insensitive LIKE)
    Page<BasicPolicy> findByPolicyNameContainingIgnoreCase(String policyName, Pageable pageable);
    Page<BasicPolicy> findByStatusAndPolicyNameContainingIgnoreCase(PolicyStatus status, String policyName, Pageable pageable);
    Page<BasicPolicy> findByTypeAndPolicyNameContainingIgnoreCase(PolicyType type, String policyName, Pageable pageable);
    Page<BasicPolicy> findByStatusAndTypeAndPolicyNameContainingIgnoreCase(PolicyStatus status, PolicyType type, String policyName, Pageable pageable);
}