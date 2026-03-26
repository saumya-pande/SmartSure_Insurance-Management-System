package com.dev.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.authentication.dto.KycRequest;
import com.dev.authentication.dto.KycResponse;
import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.entity.User;
import com.dev.authentication.exception.EntityNotFoundException;
import com.dev.authentication.mapper.KycMapper;
import com.dev.authentication.repository.KycRepository;
import com.dev.authentication.repository.UserRepository;

import java.nio.file.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Kyc kyc = Kyc.builder()
                .contactNumber(request.getContactNumber())
                .documentType(request.getDocumentType())
                .documentPath(filePath.toString())
                .address(request.getAddress())
                .status(KycStatus.PENDING)
                .user(user)
                .build();

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
}
