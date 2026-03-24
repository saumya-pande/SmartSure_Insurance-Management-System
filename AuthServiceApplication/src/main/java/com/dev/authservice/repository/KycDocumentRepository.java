package com.dev.authservice.repository;

import com.dev.authservice.entity.DocumentType;
import com.dev.authservice.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByUserId(Long userId);
    boolean existsByUserIdAndDocumentType(Long userId, DocumentType documentType);
}
