package com.dev.claimsservice.service;

import com.dev.claimsservice.client.PolicyClient;
import com.dev.claimsservice.config.RabbitMQConfig;
import com.dev.claimsservice.dto.*;
import com.dev.claimsservice.entity.*;
import com.dev.claimsservice.exception.EntityNotFoundException;
import com.dev.claimsservice.exception.InvalidOperationException;
import com.dev.claimsservice.mapper.ClaimMapper;
import com.dev.claimsservice.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClaimService {
	
	 @Autowired
	private RabbitTemplate rabbitTemplate;
    private final ClaimRepository claimRepo;
    private final ClaimDocumentRepository docRepo;
    private final PolicyClient policyClient;
    private final ClaimMapper mapper;
    
    @Value("${file.upload-dir}")
    private String UPLOAD_DIR;

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/png"
    );

    // STEP 1 — customer creates a DRAFT claim with documents
    public ClaimResponse createDraft(String email, ClaimRequest request,
                                     List<MultipartFile> files) throws Exception {

        // verify policy exists and belongs to customer and is ACTIVE
        CustomerPolicyResponse policy = policyClient.getPolicyById(
                request.getCustomerPolicyId(), email, "ROLE_CUSTOMER");

        if (!policy.getCustomerEmail().equals(email)) {
            throw new InvalidOperationException("Policy does not belong to this customer");
        }

        if (policy.getStatus() != PurchaseStatus.ACTIVE) {
            throw new InvalidOperationException("Policy is not active");
        }

        // save documents
        List<ClaimDocument> documents = saveDocuments(files);

        Claim claim = Claim.builder()
                .customerEmail(email)
                .customerPolicyId(request.getCustomerPolicyId())
                .claimAmount(request.getClaimAmount())
                .status(ClaimStatus.DRAFT)
                .documents(new ArrayList<>())
                .build();

        Claim saved = claimRepo.save(claim);

        // link documents to claim
        documents.forEach(doc -> {
            doc.setClaim(saved);
            docRepo.save(doc);
        });

        saved.setDocuments(documents);
        return mapper.toResponse(saved);
    }

    // STEP 2 — customer submits the draft
    public ClaimResponse submit(String email, Long claimId) {
        Claim claim = getClaimForCustomer(email, claimId);

        if (claim.getStatus() != ClaimStatus.DRAFT) {
            throw new InvalidOperationException("Only DRAFT claims can be submitted");
        }

        claim.setStatus(ClaimStatus.SUBMITTED);
        
     // Publish CLAIM_SUBMITTED event to RabbitMQ
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("claimId", claimId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_CLAIM_SUBMITTED, payload);
        
        return mapper.toResponse(claimRepo.save(claim));
    }

    // ADMIN — move to UNDER_REVIEW
    public ClaimResponse startReview(Long claimId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim", "id", claimId));

        if (claim.getStatus() != ClaimStatus.SUBMITTED) {
            throw new InvalidOperationException("Only SUBMITTED claims can be reviewed");
        }

        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        return mapper.toResponse(claimRepo.save(claim));
    }

    // ADMIN — approve or reject
    public ClaimResponse updateStatus(Long claimId, ClaimStatus status) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim", "id", claimId));

        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new InvalidOperationException("Claim must be UNDER_REVIEW to approve or reject");
        }

        if (status != ClaimStatus.APPROVED && status != ClaimStatus.REJECTED) {
            throw new InvalidOperationException("Status must be APPROVED or REJECTED");
        }

        claim.setStatus(status);
        return mapper.toResponse(claimRepo.save(claim));
    }

    // ADMIN — close a claim
    public ClaimResponse close(Long claimId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim", "id", claimId));

        if (claim.getStatus() != ClaimStatus.APPROVED &&
            claim.getStatus() != ClaimStatus.REJECTED) {
            throw new InvalidOperationException("Only APPROVED or REJECTED claims can be closed");
        }

        claim.setStatus(ClaimStatus.CLOSED);
        return mapper.toResponse(claimRepo.save(claim));
    }

    // CUSTOMER — get their own claims
    public Page<ClaimResponse> getMyClaims(String email, Pageable pageable) {
        return claimRepo.findByCustomerEmail(email, pageable).map(mapper::toResponse);
    }

    // ADMIN — get all claims
    public Page<ClaimResponse> getAll(Pageable pageable) {
        return claimRepo.findAll(pageable).map(mapper::toResponse);
    }

    // ── helpers ──────────────────────────────────────────────

    private Claim getClaimForCustomer(String email, Long claimId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim", "id", claimId));
        if (!claim.getCustomerEmail().equals(email)) {
            throw new InvalidOperationException("Claim does not belong to this customer");
        }
        return claim;
    }

    private List<ClaimDocument> saveDocuments(List<MultipartFile> files) throws Exception {
        List<ClaimDocument> docs = new ArrayList<>();

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                throw new InvalidOperationException("Invalid file type: " + file.getOriginalFilename()
                        + ". Only PDF, JPEG, PNG allowed.");
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            docs.add(ClaimDocument.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .fileType(contentType)
                    .build());
        }

        return docs;
    }
}
