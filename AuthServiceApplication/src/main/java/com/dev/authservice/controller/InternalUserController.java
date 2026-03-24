package com.dev.authservice.controller;

import com.dev.authservice.entity.KycStatus;
import com.dev.authservice.entity.User;
import com.dev.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/auth/users")
public class InternalUserController {

    @Autowired
    private UserRepository userRepository;

    /** Called by Policy Service Feign to check KYC status before purchase */
    @GetMapping("/{userId}/kyc-status")
    public ResponseEntity<String> getKycStatus(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(user.getKycStatus().name()))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Admin: get all users */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /** Admin: get a single user by ID */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Admin: get all users with kycStatus = SUBMITTED (need review) */
    @GetMapping("/kyc/pending")
    public ResponseEntity<List<User>> getPendingKycUsers() {
        return ResponseEntity.ok(userRepository.findByKycStatus(KycStatus.SUBMITTED));
    }

    /** Admin: get all users by KYC status */
    @GetMapping("/kyc")
    public ResponseEntity<List<User>> getUsersByKycStatus(@RequestParam KycStatus status) {
        return ResponseEntity.ok(userRepository.findByKycStatus(status));
    }

    /** Admin: count of users by each KYC status — for dashboard */
    @GetMapping("/kyc/counts")
    public ResponseEntity<Map<String, Long>> getKycCounts() {
        return ResponseEntity.ok(Map.of(
            "PENDING", userRepository.findByKycStatus(KycStatus.PENDING).stream().count(),
            "SUBMITTED", userRepository.findByKycStatus(KycStatus.SUBMITTED).stream().count(),
            "VERIFIED", userRepository.findByKycStatus(KycStatus.VERIFIED).stream().count(),
            "REJECTED", userRepository.findByKycStatus(KycStatus.REJECTED).stream().count()
        ));
    }
}
