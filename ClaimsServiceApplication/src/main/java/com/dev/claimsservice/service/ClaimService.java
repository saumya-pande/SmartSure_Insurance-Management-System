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
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {
	
	private final RabbitTemplate rabbitTemplate;
    private final ClaimRepository claimRepo;
    private final ClaimDocumentRepository docRepo;
    private final PolicyClient policyClient;
    private final ClaimMapper mapper;
    
    @Value("${file.upload-dir}")
    private String uploadDir;

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

        if (claimRepo.existsByCustomerPolicyIdAndStatusNot(request.getCustomerPolicyId(), ClaimStatus.REJECTED)) {
            throw new InvalidOperationException("A claim for this policy has already been submitted and is active.");
        }

        // save documents
        List<ClaimDocument> documents = saveDocuments(email, files);

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
    @CacheEvict(cacheNames = "claims", allEntries = true)
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
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_CLAIM_SUBMITTED, payload);
        } catch (Exception e) {
            log.error("Failed to send claim submission event to RabbitMQ for claim ID: {}. Error: {}", claimId, e.getMessage());
        }
        
        return mapper.toResponse(claimRepo.save(claim));
    }

    // CUSTOMER — update draft claim
    @CacheEvict(cacheNames = "claims", allEntries = true)
    public ClaimResponse updateDraft(String email, Long id, ClaimRequest request,
                                     List<MultipartFile> files) throws Exception {
        Claim claim = getClaimForCustomer(email, id);

        if (claim.getStatus() != ClaimStatus.DRAFT) {
            throw new InvalidOperationException("Only DRAFT claims can be updated");
        }

        claim.setClaimAmount(request.getClaimAmount());

        if (files != null && !files.isEmpty()) {
            // Remove old documents from DB (files stay on disk for now, could be improved)
            docRepo.deleteAll(claim.getDocuments());
            
            List<ClaimDocument> newDocs = saveDocuments(email, files);
            newDocs.forEach(doc -> {
                doc.setClaim(claim);
                docRepo.save(doc);
            });
            claim.setDocuments(newDocs);
        }

        return mapper.toResponse(claimRepo.save(claim));
    }

    // ADMIN — handle all status transitions
    @CacheEvict(cacheNames = "claims", allEntries = true)
    public ClaimResponse updateStatus(Long claimId, ClaimStatus status) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim", "id", claimId));

        if (status == ClaimStatus.UNDER_REVIEW) {
            if (claim.getStatus() != ClaimStatus.SUBMITTED) {
                throw new InvalidOperationException("Only SUBMITTED claims can be reviewed");
            }
        } else if (status == ClaimStatus.APPROVED || status == ClaimStatus.REJECTED) {
            if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
                throw new InvalidOperationException("Claim must be UNDER_REVIEW to approve or reject");
            }
        } else if (status == ClaimStatus.CLOSED) {
            if (claim.getStatus() != ClaimStatus.APPROVED && claim.getStatus() != ClaimStatus.REJECTED) {
                throw new InvalidOperationException("Only APPROVED or REJECTED claims can be closed");
            }
        } else {
            throw new InvalidOperationException("Invalid target status for admin update");
        }

        claim.setStatus(status);
        Claim saved = claimRepo.save(claim);

        if (status == ClaimStatus.APPROVED || status == ClaimStatus.REJECTED) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("email", saved.getCustomerEmail());
            payload.put("claimId", saved.getId());
            payload.put("status", status.name());
            payload.put("amount", saved.getClaimAmount());
            try {
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_CLAIM_STATUS_UPDATED, payload);
            } catch (Exception e) {
                log.error("Failed to send claim status update event to RabbitMQ for claim ID: {}. Status: {}. Error: {}", saved.getId(), status, e.getMessage());
            }
        }

        return mapper.toResponse(saved);
    }

    // CUSTOMER — get their own claims
    public Page<ClaimResponse> getMyClaims(String email, Pageable pageable) {
        return claimRepo.findByCustomerEmail(email, pageable).map(mapper::toResponse);
    }

    // ADMIN — get all claims filters
    public Page<ClaimResponse> getAll(ClaimStatus status, String email,
            String startDateStr, String endDateStr, Pageable pageable) {
        LocalDateTime startDate = (startDateStr != null && !startDateStr.isBlank())
                ? LocalDate.parse(startDateStr).atStartOfDay() : null;
        LocalDateTime endDate = (endDateStr != null && !endDateStr.isBlank())
                ? LocalDate.parse(endDateStr).atTime(23, 59, 59) : null;

        // If date filters are provided, use the flexible query
        if (startDate != null || endDate != null) {
            return claimRepo.findByFilters(status, email, startDate, endDate, pageable)
                    .map(mapper::toResponse);
        }

        // Original filter logic (no dates)
        if (status != null && email != null) {
            return claimRepo.findByStatusAndCustomerEmailContaining(status, email, pageable).map(mapper::toResponse);
        } else if (status != null) {
            return claimRepo.findByStatus(status, pageable).map(mapper::toResponse);
        } else if (email != null) {
            return claimRepo.findByCustomerEmailContaining(email, pageable).map(mapper::toResponse);
        }
        return claimRepo.findAll(pageable).map(mapper::toResponse);
    }

    // ADMIN — metrics
    @Cacheable(cacheNames = "claims", key = "'counts'")
    public Map<String, Long> getClaimCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", claimRepo.count());
        for (ClaimStatus status : ClaimStatus.values()) {
            counts.put(status.name(), claimRepo.countByStatus(status));
        }
        return counts;
    }

    // ADMIN — payouts
    @Cacheable(cacheNames = "claims", key = "'payouts'")
    public Map<String, Double> getApprovedPayouts() {
        Double total = claimRepo.sumClaimAmountByStatus(ClaimStatus.APPROVED);
        Map<String, Double> result = new HashMap<>();
        result.put("total", total != null ? total : 0.0);
        return result;
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

    /**
     * Save documents to disk with email-based naming.
     * Stores only the filename in the entity, not the full path.
     * Naming convention: {emailPrefix}_{timestamp}_{originalFilename}
     */
    private List<ClaimDocument> saveDocuments(String email, List<MultipartFile> files) throws Exception {
        List<ClaimDocument> docs = new ArrayList<>();

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String emailPrefix = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "_");

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                throw new InvalidOperationException("Invalid file type: " + file.getOriginalFilename()
                        + ". Only PDF, JPEG, PNG allowed.");
            }

            String fileName = emailPrefix + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Store only the filename in the entity
            docs.add(ClaimDocument.builder()
                    .fileName(fileName)
                    .filePath(fileName)
                    .fileType(contentType)
                    .build());
        }

        return docs;
    }

    /**
     * Resolve a stored filename (or legacy full path) to a Resource.
     */
    public Resource getDocumentAsResource(Long docId) throws MalformedURLException {
        ClaimDocument doc = docRepo.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("ClaimDocument", "id", docId));

        Path filePath = resolveFilePath(doc.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            throw new EntityNotFoundException("File", "path", doc.getFilePath());
        }
        return resource;
    }

    public String getDocumentContentType(Long docId) {
        ClaimDocument doc = docRepo.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("ClaimDocument", "id", docId));
        return doc.getFileType() != null ? doc.getFileType() : "application/octet-stream";
    }

    /**
     * Resolves a stored filename (or legacy full path) to a physical file path.
     * Supports both new format (filename only) and legacy format (absolute path).
     */
    private Path resolveFilePath(String documentPath) {
        Path path = Paths.get(documentPath);
        if (path.isAbsolute()) {
            // Legacy: full path stored in DB
            return path;
        }
        // New: filename only — resolve against upload directory
        return Paths.get(uploadDir).resolve(documentPath);
    }
}
