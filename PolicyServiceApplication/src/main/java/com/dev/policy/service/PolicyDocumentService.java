package com.dev.policy.service;

import com.dev.policy.entity.Policy;
import com.dev.policy.entity.PolicyDocument;
import com.dev.policy.entity.enums.AssetDocumentType;
import com.dev.policy.entity.enums.PolicyStatus;
import com.dev.policy.exception.ResourceNotFoundException;
import com.dev.policy.repository.PolicyDocumentRepository;
import com.dev.policy.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public class PolicyDocumentService {

    @Autowired
    private PolicyDocumentRepository documentRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyFileStorageService fileStorageService;

    public PolicyDocument uploadDocument(Long policyId, AssetDocumentType docType, MultipartFile file) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + policyId));

        if (policy.getStatus() == PolicyStatus.ACTIVE) {
            throw new RuntimeException("Policy is already ACTIVE. No further documents needed.");
        }

        if (documentRepository.existsByPolicyIdAndDocumentType(policyId, docType)) {
            throw new RuntimeException("Document of type " + docType + " is already uploaded for this policy.");
        }

        String filePath = fileStorageService.storeFile(file, policyId, docType.name());

        PolicyDocument document = PolicyDocument.builder()
                .policy(policy)
                .documentType(docType)
                .filePath(filePath)
                .build();

        return documentRepository.save(document);
    }
    
    public List<PolicyDocument> getDocumentsForPolicy(Long policyId) {
        return documentRepository.findByPolicyId(policyId);
    }
}
