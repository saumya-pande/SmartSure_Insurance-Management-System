package com.dev.authentication.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dev.authentication.dto.KycRequest;
import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService service;
    
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload KYC document")
    public ResponseEntity<String> upload(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @RequestParam String contactNumber,   
            @RequestParam String address,         
            @RequestParam String documentType,    
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        KycRequest request = new KycRequest();
        request.setContactNumber(contactNumber);
        request.setAddress(address);
        request.setDocumentType(documentType);
        service.upload(email, request, file);
        return ResponseEntity.ok("KYC uploaded successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestParam KycStatus status) {
        service.updateStatus(id, status);
    }
    
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/{id}")
//    public ResponseEntity<Kyc> getById(@PathVariable int id) {
//    	ResponseEntiservice.getById();
//    }
}