package com.dev.authentication.controller;


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
public class AuthController {

    private final AuthService  service;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(service.register(req));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String header) {
        
        return ResponseEntity.ok(service.logout(header.substring(7)));
    }
    
    @GetMapping("/validate")
    public boolean validate(@RequestParam String token) {
        return !service.isRevoked(token);
    }
}