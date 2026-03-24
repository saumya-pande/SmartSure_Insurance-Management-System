package com.dev.claims.service;

import com.dev.claims.client.PolicyServiceClient;
import com.dev.claims.dto.ClaimResponse;
import com.dev.claims.dto.InitiateClaimRequest;
import com.dev.claims.entity.Claim;
import com.dev.claims.entity.ClaimDocument;
import com.dev.claims.entity.enums.ClaimDocumentType;
import com.dev.claims.entity.enums.ClaimStatus;
import com.dev.claims.repository.ClaimDocumentRepository;
import com.dev.claims.repository.ClaimRepository;
import com.dev.claims.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimDocumentRepository claimDocumentRepository;

    @Autowired
    private PolicyServiceClient policyServiceClient;

    @Autowired
    private ClaimFileStorageService fileStorageService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public ClaimResponse initiateClaim(InitiateClaimRequest request, Long userId, String userEmail) {
        // Verify the policy is ACTIVE via Feign
        String policyStatus;
        try {
            policyStatus = policyServiceClient.getPolicyStatus(request.getPolicyId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to verify policy status. Please try again.");
        }

        if (!"ACTIVE".equals(policyStatus)) {
            throw new IllegalArgumentException(
                "Policy " + request.getPolicyId() + " is not ACTIVE (current status: " + policyStatus + "). Only ACTIVE policies can be claimed."
            );
        }

        Claim claim = Claim.builder()
                .policyId(request.getPolicyId())
                .userId(userId)
                .userEmail(userEmail)
                .incidentType(request.getIncidentType())
                .incidentDate(request.getIncidentDate())
                .description(request.getDescription())
                .status(ClaimStatus.SUBMITTED)
                .build();

        Claim saved = claimRepository.save(claim);

        // Publish CLAIM_SUBMITTED event to RabbitMQ
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", userEmail);
        payload.put("claimId", saved.getId());
        payload.put("policyId", request.getPolicyId());
        payload.put("incidentType", request.getIncidentType().name());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_CLAIM_SUBMITTED, payload);

        return convertToResponse(saved);
    }

    public ClaimDocument uploadDocument(Long claimId, ClaimDocumentType docType, MultipartFile file) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + claimId));

        if (claimDocumentRepository.existsByClaimIdAndDocumentType(claimId, docType)) {
            throw new RuntimeException("Document of type " + docType + " already uploaded for this claim.");
        }

        String filePath = fileStorageService.storeFile(file, claimId, docType.name());

        ClaimDocument doc = ClaimDocument.builder()
                .claim(claim)
                .documentType(docType)
                .filePath(filePath)
                .build();

        return claimDocumentRepository.save(doc);
    }

    public ClaimResponse updateStatus(Long claimId, ClaimStatus status, String adminRemarks) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + claimId));
        claim.setStatus(status);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setAdminRemarks(adminRemarks);
        return convertToResponse(claimRepository.save(claim));
    }

    public ClaimResponse getClaimById(Long claimId) {
        return claimRepository.findById(claimId)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + claimId));
    }

    public List<ClaimResponse> getMyClaims(Long userId) {
        return claimRepository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ClaimResponse convertToResponse(Claim c) {
        return ClaimResponse.builder()
                .id(c.getId())
                .policyId(c.getPolicyId())
                .userId(c.getUserId())
                .userEmail(c.getUserEmail())
                .incidentType(c.getIncidentType())
                .incidentDate(c.getIncidentDate())
                .description(c.getDescription())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .reviewedAt(c.getReviewedAt())
                .adminRemarks(c.getAdminRemarks())
                .build();
    }
}
