package com.dev.policy.controller;

import com.dev.policy.dto.PolicyResponse;
import com.dev.policy.entity.enums.AssetDocumentType;
import com.dev.policy.entity.enums.PolicyStatus;
import com.dev.policy.service.PolicyDocumentService;
import com.dev.policy.service.PolicyPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/policies")
public class PolicyDocumentController {

    @Autowired
    private PolicyDocumentService documentService;

    @Autowired
    private PolicyPurchaseService policyPurchaseService;

    @PostMapping("/{id}/documents")
    public ResponseEntity<?> uploadPolicyDocument(
            @PathVariable Long id,
            @RequestParam("documentType") AssetDocumentType type,
            @RequestParam("file") MultipartFile file) {
        try {
            documentService.uploadDocument(id, type, file);
            return ResponseEntity.ok("Document " + type + " uploaded successfully for Policy " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
