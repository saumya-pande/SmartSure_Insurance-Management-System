package com.dev.policy.repository;


import com.dev.policy.entity.BasicPolicy;
import com.dev.policy.entity.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasicPolicyRepository extends JpaRepository<BasicPolicy, Long> {
    Page<BasicPolicy> findByStatus(PolicyStatus status, Pageable pageable);
    long countByStatus(PolicyStatus status);
}