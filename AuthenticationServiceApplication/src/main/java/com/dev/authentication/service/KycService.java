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
    private String UPLOAD_DIR;

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

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        kyc.setDocumentPath(filePath.toString());
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
    public Resource getFileAsResource(String email) throws MalformedURLException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));

        Kyc kyc = repo.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("KYC", "user", email));

        Path filePath = Paths.get(kyc.getDocumentPath());
        return new UrlResource(filePath.toUri());
    }

    public Resource getFileAsResourceById(Long id) throws MalformedURLException {
        Kyc kyc = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("KYC", "id", id));

        Path filePath = Paths.get(kyc.getDocumentPath());
        return new UrlResource(filePath.toUri());
    }
}
