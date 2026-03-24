package com.dev.policy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PolicyFileStorageService {

    private final Path fileStorageLocation;

    public PolicyFileStorageService(@Value("${file.upload-dir:./uploads/policy-documents/}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, Long policyId, String docType) {
        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            String newFileName = "policy_" + policyId + "_" + docType + "_" + UUID.randomUUID().toString().substring(0, 5) + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            
            Files.copy(file.getInputStream(), targetLocation);
            return targetLocation.toString();

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}
