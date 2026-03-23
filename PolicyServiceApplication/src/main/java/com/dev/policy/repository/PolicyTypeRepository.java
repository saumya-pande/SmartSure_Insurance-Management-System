package com.dev.policy.repository;

import com.dev.policy.entity.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyTypeRepository extends JpaRepository<PolicyType, Long> {
}
