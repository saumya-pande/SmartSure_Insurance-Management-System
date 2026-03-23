package com.dev.policy.repository;

import com.dev.policy.entity.Premium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PremiumRepository extends JpaRepository<Premium, Long> {
    List<Premium> findByPolicyId(Long policyId);
}
