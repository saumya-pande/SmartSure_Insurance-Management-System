package com.dev.authservice.controller;

import com.dev.authservice.entity.DocumentType;
import com.dev.authservice.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth/kyc")
public class KycController {

    @Autowired
    private KycService kycService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadKycDocument(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam("documentType") DocumentType type,
            @RequestParam("file") MultipartFile file) {

        if (userId == null) {
            return ResponseEntity.status(401).body("Missing X-User-Id header");
        }

        try {
            kycService.uploadDocument(userId, type, file);
            return ResponseEntity.ok("Document " + type + " uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
