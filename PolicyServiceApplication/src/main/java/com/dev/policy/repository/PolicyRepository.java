package com.dev.policy.repository;

import com.dev.policy.entity.Policy;
import com.dev.policy.entity.enums.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByUserId(Long userId);
    List<Policy> findByStatus(PolicyStatus status);
    List<Policy> findByUserIdAndStatus(Long userId, PolicyStatus status);
}
