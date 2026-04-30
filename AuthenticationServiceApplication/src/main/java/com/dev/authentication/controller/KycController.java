package com.dev.authentication.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dev.authentication.dto.KycRequest;
import com.dev.authentication.dto.KycResponse;

import com.dev.authentication.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

	private final KycService service;

    // CUSTOMER — upload their own KYC
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

    // CUSTOMER — view their own KYC
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    @Operation(summary = "Get my KYC status")
    public ResponseEntity<KycResponse> getMyKyc(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email
    ) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    // ADMIN — view all KYC submissions with pagination
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Get all KYC submissions (admin)")
    public ResponseEntity<Page<KycResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size, Sort.by(sortBy))));
    }

    // View KYC file by ID — customer can only access their own, admin can access any
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/{id}/file")
    @Operation(summary = "View KYC document file")
    public ResponseEntity<Resource> getKycFileById(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role
    ) throws MalformedURLException {
        // Customers can only view their own KYC file
        if (role != null && role.contains("CUSTOMER")) {
            service.verifyOwnership(id, email);
        }
        Resource resource = service.getFileAsResourceById(id);
        String contentType = service.getFileContentTypeById(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
