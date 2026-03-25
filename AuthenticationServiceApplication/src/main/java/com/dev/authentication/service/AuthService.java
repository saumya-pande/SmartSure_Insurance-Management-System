package com.dev.authentication.service;


import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev.authentication.dto.AuthResponse;
import com.dev.authentication.dto.LoginRequest;
import com.dev.authentication.dto.RegisterRequest;
import com.dev.authentication.entity.Role;
import com.dev.authentication.entity.User;
import com.dev.authentication.repository.UserRepository;
import com.dev.authentication.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthResponse register(RegisterRequest request) {

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        repo.save(user);

        return generateTokens(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow();

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        return new AuthResponse(
                jwt.generateToken(user),
                jwt.generateRefreshToken(user),
                user.getRole().name()
        );
    }
    
    private final Set<String> revokedTokens = new HashSet<>();

    public void logout(String token) {
        revokedTokens.add(token);
    }

    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }
    
    public boolean isTokenValid(String token) {
        return !revokedTokens.contains(token);
    }
}