package com.dev.claimsservice.repository;


import com.dev.claimsservice.entity.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {}