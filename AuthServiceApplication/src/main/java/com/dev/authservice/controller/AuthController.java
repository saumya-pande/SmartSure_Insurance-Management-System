package com.dev.authservice.controller;

import com.dev.authservice.dto.JwtResponse;
import com.dev.authservice.dto.LoginRequest;
import com.dev.authservice.dto.RegisterRequest;
import com.dev.authservice.entity.RevokedToken;
import com.dev.authservice.entity.Role;
import com.dev.authservice.entity.User;
import com.dev.authservice.repository.RevokedTokenRepository;
import com.dev.authservice.repository.UserRepository;
import com.dev.authservice.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.dev.authservice.config.RabbitMQConfig;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        
        if (userOpt.isPresent() && encoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            String roleStr = "ROLE_" + user.getRole().name();
            
            String token = jwtUtil.generateToken(user.getEmail(), roleStr, user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
            
            return ResponseEntity.ok(new JwtResponse(token, refreshToken, roleStr));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Bad credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        // Default role 
        Role role = Role.CUSTOMER;
        if (signUpRequest.getRole() != null && signUpRequest.getRole().equalsIgnoreCase("admin")) {
            role = Role.ADMIN;
        }

        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .phone(signUpRequest.getPhone())
                .address(signUpRequest.getAddress())
                .role(role)
                .build();
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String tokenHeader) {
        if (StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            
            if (revokedTokenRepository.findById(token).isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has been revoked.");
            }

            if (jwtUtil.isTokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired. Please login again.");
            }
            
            String email = jwtUtil.getUsernameFromToken(token);
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String roleStr = "ROLE_" + user.getRole().name();
                return ResponseEntity.ok(new JwtResponse(
                        jwtUtil.generateToken(user.getEmail(), roleStr, user.getId()),
                        token,
                        roleStr
                ));
            }
        }
        return ResponseEntity.badRequest().body("Invalid refresh token.");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            RevokedToken revokedToken = new RevokedToken(token);
            revokedTokenRepository.save(revokedToken);
            return ResponseEntity.ok("Logged out successfully.");
        }
        return ResponseEntity.badRequest().body("No token provided to logout.");
    }
}
