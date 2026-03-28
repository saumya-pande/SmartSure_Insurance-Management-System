package com.dev.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dev.authentication.dto.AuthResponse;
import com.dev.authentication.dto.LoginRequest;
import com.dev.authentication.dto.RegisterRequest;
import com.dev.authentication.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, logout, and token validation")
public class AuthController {

    private final AuthService  service;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(service.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(service.login(req));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke the current JWT token")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String header) {
        return ResponseEntity.ok(service.logout(header.substring(7)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(service.refresh(refreshToken));
    }
    
    @GetMapping("/validate")
    @Operation(summary = "Validate if a JWT token is still active")
    public ResponseEntity<Boolean> validate(@RequestParam String token) {
        return ResponseEntity.ok(!service.isRevoked(token));
    }
}