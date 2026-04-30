package com.dev.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.authentication.dto.KycRequest;
import com.dev.authentication.dto.KycResponse;
import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.entity.User;
import com.dev.authentication.exception.DuplicateResourceException;
import com.dev.authentication.exception.EntityNotFoundException;
import com.dev.authentication.mapper.KycMapper;
import com.dev.authentication.repository.KycRepository;
import com.dev.authentication.repository.UserRepository;

import java.nio.file.*;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.net.MalformedURLException;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycRepository repo;
    private final UserRepository userRepo;
    private final KycMapper mapper;

    @Value("${app.upload-dir}")
    private String uploadDir;

    /**
     * Upload or re-upload (if rejected) a KYC document.
     * Stores only the filename in the database; the directory is resolved at runtime from config.
     * File naming convention: {emailPrefix}_{timestamp}_{originalFilename}
     */
    public void upload(String email, KycRequest request, MultipartFile file) throws Exception {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));

        // Check if user already has a KYC submission
        Optional<Kyc> existingKyc = repo.findByUserId(user.getId());
        Kyc kyc;

        if (existingKyc.isPresent()) {
            kyc = existingKyc.get();
            if (kyc.getStatus() != KycStatus.REJECTED) {
                throw new DuplicateResourceException("KYC", "user", email);
            }
            // Update existing rejected KYC
            kyc.setContactNumber(request.getContactNumber());
            kyc.setDocumentType(request.getDocumentType());
            kyc.setAddress(request.getAddress());
            kyc.setStatus(KycStatus.PENDING);
        } else {
            // Create new KYC
            kyc = Kyc.builder()
                    .user(user)
                    .status(KycStatus.PENDING)
                    .contactNumber(request.getContactNumber())
                    .documentType(request.getDocumentType())
                    .address(request.getAddress())
                    .build();
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // File naming: emailPrefix_timestamp_originalFilename
        String emailPrefix = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = emailPrefix + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Store only the filename, not the full path
        kyc.setDocumentPath(fileName);
        repo.save(kyc);
    }

    public KycResponse getByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));

        Kyc kyc = repo.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("KYC", "user", email));

        return mapper.toResponse(kyc);
    }

    public Page<KycResponse> getAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    public void updateStatus(Long id, KycStatus status) {
        Kyc kyc = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("KYC", "id", id));
        kyc.setStatus(status);
        repo.save(kyc);
    }

    /**
     * Verify that the KYC record belongs to the given email.
     * Throws EntityNotFoundException if the KYC doesn't belong to this user.
     */
    public void verifyOwnership(Long kycId, String email) {
        Kyc kyc = repo.findById(kycId)
                .orElseThrow(() -> new EntityNotFoundException("KYC", "id", kycId));
        if (!kyc.getUser().getEmail().equals(email)) {
            throw new EntityNotFoundException("KYC", "id", kycId);
        }
    }

    /**
     * Resolve a KYC document file for the given customer email.
     * Only returns their own file — enforced by the email lookup.
     */
    public Resource getFileAsResource(String email) throws MalformedURLException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));

        Kyc kyc = repo.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("KYC", "user", email));

        return resolveResource(kyc.getDocumentPath());
    }

    /**
     * Resolve a KYC document file by KYC ID (admin use).
     */
    public Resource getFileAsResourceById(Long id) throws MalformedURLException {
        Kyc kyc = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("KYC", "id", id));

        return resolveResource(kyc.getDocumentPath());
    }

    public String getFileContentType(String documentPath) {
        try {
            Path filePath = resolveFilePath(documentPath);
            String contentType = Files.probeContentType(filePath);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (Exception ex) {
            return "application/octet-stream";
        }
    }

    public String getFileContentTypeById(Long id) {
        Kyc kyc = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("KYC", "id", id));
        return getFileContentType(kyc.getDocumentPath());
    }

    // ── helpers ──────────────────────────────────────────────

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

    private Resource resolveResource(String documentPath) throws MalformedURLException {
        Path filePath = resolveFilePath(documentPath);
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            throw new EntityNotFoundException("File", "path", documentPath);
        }
        return resource;
    }
}
