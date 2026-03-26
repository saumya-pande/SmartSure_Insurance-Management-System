package com.dev.authentication.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository repo;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtUtil jwt;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private AuthService service;

    private User sampleUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encoded_password")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("register()")
    class Register {
        @Test
        @DisplayName("should register successfully and send rabbitmq message")
        void register_success() {
            when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
            when(encoder.encode(anyString())).thenReturn("encoded_password");
            when(repo.save(any(User.class))).thenReturn(sampleUser);

            String result = service.register(registerRequest);

            assertThat(result).isEqualTo("User registered successfully!");
            verify(repo).save(any(User.class));
            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_REGISTER), any(java.util.Map.class));
        }

        @Test
        @DisplayName("should throw error if email already exists")
        void register_emailExists() {
            when(repo.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> service.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("email");

            verify(repo, never()).save(any());
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {
        @Test
        @DisplayName("should return tokens on valid credentials")
        void login_success() {
            when(repo.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
            when(encoder.matches("password123", "encoded_password")).thenReturn(true);
            when(jwt.generateToken(sampleUser)).thenReturn("access-token");
            when(jwt.generateRefreshToken(sampleUser)).thenReturn("refresh-token");

            AuthResponse response = service.login(loginRequest);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getRole()).isEqualTo("CUSTOMER");
        }

        @Test
        @DisplayName("should throw error if user not found")
        void login_userNotFound() {
            when(repo.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.login(loginRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("should throw error on wrong password")
        void login_wrongPassword() {
            when(repo.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
            when(encoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> service.login(loginRequest))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("Invalid credentials");
        }
    }

    @Nested
    @DisplayName("token management")
    class Tokens {
        @Test
        @DisplayName("should logout and revoke token")
        void logout_success() {
            String token = "some.jwt.token";
            String response = service.logout(token);
            
            assertThat(response).isEqualTo("Logged out successfully!");
            assertThat(service.isRevoked(token)).isTrue();
            assertThat(service.isTokenValid(token)).isFalse();
        }
    }
}
