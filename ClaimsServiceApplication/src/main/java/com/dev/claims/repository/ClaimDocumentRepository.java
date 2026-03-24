package com.dev.claims.repository;

import com.dev.claims.entity.ClaimDocument;
import com.dev.claims.entity.enums.ClaimDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {
    List<ClaimDocument> findByClaimId(Long claimId);
    boolean existsByClaimIdAndDocumentType(Long claimId, ClaimDocumentType documentType);
}
