package com.dev.authentication.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.service.KycService;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService service;

    @PostMapping("/upload")
    public void upload(
            @RequestParam Long userId,
            @RequestParam String type,
            @RequestParam String address,
            @RequestParam MultipartFile file
    ) throws Exception {
        service.upload(userId, type, address, file);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestParam KycStatus status) {
        service.updateStatus(id, status);
    }
}