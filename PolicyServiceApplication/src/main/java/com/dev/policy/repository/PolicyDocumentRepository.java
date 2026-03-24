package com.dev.policy.repository;

import com.dev.policy.entity.PolicyDocument;
import com.dev.policy.entity.enums.AssetDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyDocumentRepository extends JpaRepository<PolicyDocument, Long> {
    List<PolicyDocument> findByPolicyId(Long policyId);
    boolean existsByPolicyIdAndDocumentType(Long policyId, AssetDocumentType documentType);
}
