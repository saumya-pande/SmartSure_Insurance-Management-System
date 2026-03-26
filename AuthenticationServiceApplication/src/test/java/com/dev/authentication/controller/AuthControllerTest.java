package com.dev.authentication.controller;

import com.dev.authentication.dto.AuthResponse;
import com.dev.authentication.dto.LoginRequest;
import com.dev.authentication.dto.RegisterRequest;
import com.dev.authentication.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev.authentication.security.SecurityConfig;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean AuthService service;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("Password@123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("Password@123");
    }

    @Nested
    @DisplayName("register endpoint")
    class Register {
        @Test
        @DisplayName("should register successfully and return 200")
        void register_success() throws Exception {
            when(service.register(any(RegisterRequest.class))).thenReturn("User registered successfully!");

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User registered successfully!"));
        }

        @Test
        @DisplayName("should return 400 when validation fails")
        void register_invalid() throws Exception {
            registerRequest.setEmail("invalid-email");

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.email").exists());
        }
    }

    @Nested
    @DisplayName("login endpoint")
    class Login {
        @Test
        @DisplayName("should login successfully and return 200")
        void login_success() throws Exception {
            AuthResponse response = new AuthResponse("access-token", "refresh-token", "CUSTOMER");
            when(service.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("access-token"))
                    .andExpect(jsonPath("$.role").value("CUSTOMER"));
        }

        @Test
        @DisplayName("should return 400 when validation fails")
        void login_invalid() throws Exception {
            loginRequest.setEmail("");

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.email").exists());
        }
    }

    @Nested
    @DisplayName("logout endpoint")
    class Logout {
        @Test
        @org.springframework.security.test.context.support.WithMockUser
        @DisplayName("should logout successfully and return 200")
        void logout_success() throws Exception {
            when(service.logout("my-token")).thenReturn("Logged out successfully!");

            mockMvc.perform(post("/api/auth/logout")
                            .with(csrf())
                            .header("Authorization", "Bearer my-token"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Logged out successfully!"));
        }
    }

    @Nested
    @DisplayName("validate endpoint")
    class Validate {
        @Test
        @DisplayName("should return true if token is valid")
        void validate_success() throws Exception {
            when(service.isRevoked("my-token")).thenReturn(false);

            mockMvc.perform(get("/api/auth/validate")
                            .param("token", "my-token"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }
    }
}
