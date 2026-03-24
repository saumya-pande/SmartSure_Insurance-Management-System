package com.dev.authservice.service;

import com.dev.authservice.entity.DocumentType;
import com.dev.authservice.entity.KycDocument;
import com.dev.authservice.entity.KycStatus;
import com.dev.authservice.entity.User;
import com.dev.authservice.repository.KycDocumentRepository;
import com.dev.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public class KycService {

    @Autowired
    private KycDocumentRepository kycRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public KycDocument uploadDocument(Long userId, DocumentType docType, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getKycStatus() == KycStatus.VERIFIED) {
            throw new RuntimeException("KYC is already VERIFIED.");
        }

        // Avoid duplicate active uploads of the same type
        if (kycRepository.existsByUserIdAndDocumentType(userId, docType)) {
            throw new RuntimeException("Document of type " + docType + " is already uploaded.");
        }

        String filePath = fileStorageService.storeFile(file, userId, docType.name());

        KycDocument document = KycDocument.builder()
                .user(user)
                .documentType(docType)
                .filePath(filePath)
                .build();

        KycDocument savedDoc = kycRepository.save(document);

        checkAndUpgradeKycStatus(user);

        return savedDoc;
    }

    private void checkAndUpgradeKycStatus(User user) {
        List<KycDocument> docs = kycRepository.findByUserId(user.getId());
        
        boolean hasId = docs.stream().anyMatch(d -> d.getDocumentType() == DocumentType.IDENTITY_PROOF);
        boolean hasAddress = docs.stream().anyMatch(d -> d.getDocumentType() == DocumentType.ADDRESS_PROOF);
        boolean hasPhoto = docs.stream().anyMatch(d -> d.getDocumentType() == DocumentType.PASSPORT_PHOTO);

        if (hasId && hasAddress && hasPhoto && user.getKycStatus() == KycStatus.PENDING) {
            user.setKycStatus(KycStatus.SUBMITTED);
            userRepository.save(user);
        }
    }

    public String updateKycStatus(Long userId, KycStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setKycStatus(newStatus);
        userRepository.save(user);
        
        if (newStatus == KycStatus.REJECTED) {
            List<KycDocument> docs = kycRepository.findByUserId(userId);
            kycRepository.deleteAll(docs);
        }
        
        return "KYC Status updated to " + newStatus.name();
    }
}
