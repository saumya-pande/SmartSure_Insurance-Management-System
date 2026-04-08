package com.dev.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.HashMap;
import java.util.Map;

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

import com.dev.authentication.exception.InvalidOperationException;
import com.dev.authentication.repository.UserRepository;
import com.dev.authentication.security.JwtUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @CacheEvict(cacheNames = "users", key = "#request.email")
    public String register(RegisterRequest request) {

        if (repo.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        repo.save(user);

        Map<String, String> payload = new HashMap<>();
        payload.put("email", user.getEmail());
        payload.put("name", user.getName());

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_REGISTER, payload);
        } catch (Exception e) {
            log.error("Failed to send registration event to RabbitMQ for user: {}. Error: {}", user.getEmail(),
                    e.getMessage());
        }

        return "User registered successfully!";
    }

    public AuthResponse login(LoginRequest request) {

        User user = getUserByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidOperationException("User does not exist/wrong credentials"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidOperationException("User does not exist/wrong credentials");
        }

        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        String accessToken = jwt.generateToken(user);
        String refreshToken = jwt.generateRefreshToken(user);

        // Store refresh token in Redis with same expiration as the token itself
        redisTemplate.opsForValue().set(
                "refresh_token:" + refreshToken,
                user.getEmail(),
                java.time.Duration.ofMillis(jwt.getRefreshExpiration()));

        return new AuthResponse(accessToken, refreshToken, user.getRole().name());
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwt.validate(refreshToken)) {
            throw new InvalidOperationException("Invalid refresh token");
        }

        String email = redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
        if (email == null) {
            throw new InvalidOperationException("Refresh token expired or revoked");
        }

        User user = getUserByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("User not found"));

        // Revoke the old refresh token (rotate)
        redisTemplate.delete("refresh_token:" + refreshToken);

        // Generate new pair
        return generateTokens(user);
    }

    public String logout(String token) {
        // Revoke access token
        redisTemplate.opsForValue().set("revoked:" + token, "true", java.time.Duration.ofHours(24));

        // revokes the access token.
        return "Logged out successfully!";
    }

    public boolean isRevoked(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("revoked:" + token));
    }

    public boolean isTokenValid(String token) {
        return !isRevoked(token);
    }

    @Cacheable(cacheNames = "users", key = "#email")
    public java.util.Optional<User> getUserByEmail(String email) {
        return repo.findByEmail(email);
    }

}