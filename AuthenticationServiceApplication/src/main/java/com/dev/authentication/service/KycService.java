package com.dev.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.entity.User;
import com.dev.authentication.repository.KycRepository;
import com.dev.authentication.repository.UserRepository;

import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycRepository repo;
    private final UserRepository userRepo;

    private final String UPLOAD_DIR = "uploads/";

    public void upload(Long userId, String type, String address, MultipartFile file) throws Exception {

        User user = userRepo.findById(userId).orElseThrow();

        // create folder if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // generate unique filename
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path filePath = uploadPath.resolve(fileName);

        // save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Kyc kyc = Kyc.builder()
                .documentType(type)
                .documentPath(filePath.toString())
                .address(address)
                .status(KycStatus.APPROVED)
                .user(user)
                .build();

        repo.save(kyc);
    }

    public void updateStatus(Long id, KycStatus status) {
        Kyc kyc = repo.findById(id).orElseThrow();
        kyc.setStatus(status);
        repo.save(kyc);
    }
}
