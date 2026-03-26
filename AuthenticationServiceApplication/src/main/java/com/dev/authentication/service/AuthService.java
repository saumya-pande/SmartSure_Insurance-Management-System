package com.dev.authentication.service;


import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev.authentication.config.RabbitMQConfig;
import com.dev.authentication.dto.AuthResponse;
import com.dev.authentication.dto.LoginRequest;
import com.dev.authentication.dto.RegisterRequest;
import com.dev.authentication.entity.Role;
import com.dev.authentication.entity.User;
import com.dev.authentication.exception.DuplicateResourceException;
import com.dev.authentication.exception.EntityNotFoundException;
import com.dev.authentication.exception.InvalidOperationException;
import com.dev.authentication.repository.UserRepository;
import com.dev.authentication.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public String register(RegisterRequest request) {

        if (repo.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.CUSTOMER)
                .active(true)
                .build();

        repo.save(user);
        
        Map<String, String> payload = new HashMap<>();
        payload.put("email", user.getEmail());
        payload.put("name", user.getName());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_REGISTER, payload);

        return "User registered successfully!";
    }

    public AuthResponse login(LoginRequest request) {

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User", "email", request.getEmail()));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidOperationException("Invalid credentials");
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

    public String logout(String token) {
        revokedTokens.add(token);
        return "Logged out successfully!";
        }

    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }
    
    public boolean isTokenValid(String token) {
        return !revokedTokens.contains(token);
    }
}