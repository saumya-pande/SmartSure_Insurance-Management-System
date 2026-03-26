package com.dev.authentication.controller;

import com.dev.authentication.dto.KycAdminResponse;
import com.dev.authentication.dto.UserResponse;
import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.entity.Role;
import com.dev.authentication.security.HeaderAuthFilter;
import com.dev.authentication.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev.authentication.security.SecurityConfig;

@WebMvcTest(controllers = AdminController.class)
@Import({ SecurityConfig.class, HeaderAuthFilter.class })
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    AdminService service;

    private UserResponse userResponse;
    private KycAdminResponse kycAdminResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("John Doe");
        userResponse.setEmail("john@example.com");
        userResponse.setRole(Role.CUSTOMER.name());
        userResponse.setActive(true);

        kycAdminResponse = KycAdminResponse.builder()
                .id(1L).contactNumber("1234567890").address("address").documentType("Passport")
                .documentPath("path").status(KycStatus.PENDING).userEmail("john@example.com").build();
    }

    @Nested
    @DisplayName("Users Endpoints")
    class Users {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should get users successfully for ADMIN")
        void getUsers_success() throws Exception {
            Page<UserResponse> page = new PageImpl<>(List.of(userResponse));
            when(service.getUsers(isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(get("/api/admin/users")
                    .header("X-User-Email", "admin@example.com")
                    .header("X-User-Role", "ROLE_ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].email").value("john@example.com"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("should forbid CUSTOMER from getting users")
        void getUsers_forbidden() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should suspend user successfully for ADMIN")
        void toggleStatus_success() throws Exception {
            UserResponse disabled = new UserResponse();
            disabled.setId(1L);
            disabled.setName("John Doe");
            disabled.setEmail("john@example.com");
            disabled.setRole(Role.CUSTOMER.name());
            disabled.setActive(false);
            when(service.toggleStatus(eq(1L), eq(false))).thenReturn(disabled);

            mockMvc.perform(patch("/api/admin/users/1/status")
                    .param("active", "false")
                    .header("X-User-Email", "admin@example.com")
                    .header("X-User-Role", "ROLE_ADMIN")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should get user counts successfully for ADMIN")
        void getUserCounts_success() throws Exception {
            Map<String, Long> counts = Map.of("total", 100L, "active", 80L, "suspended", 20L);
            when(service.getUserCounts()).thenReturn(counts);

            mockMvc.perform(get("/api/admin/users/count")
                    .header("X-User-Email", "admin@example.com")
                    .header("X-User-Role", "ROLE_ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(100))
                    .andExpect(jsonPath("$.active").value(80));
        }
    }

    @Nested
    @DisplayName("KYC Endpoints")
    class Kyc {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should get KYC successfully for ADMIN")
        void getKyc_success() throws Exception {
            Page<KycAdminResponse> page = new PageImpl<>(List.of(kycAdminResponse));
            when(service.getKyc(isNull(), isNull(), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(get("/api/admin/kyc")
                    .header("X-User-Email", "admin@example.com")
                    .header("X-User-Role", "ROLE_ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].userEmail").value("john@example.com"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("should forbid CUSTOMER from getting KYC")
        void getKyc_forbidden() throws Exception {
            mockMvc.perform(get("/api/admin/kyc"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should update KYC status successfully for ADMIN")
        void updateKycStatus_success() throws Exception {
            KycAdminResponse approved = KycAdminResponse.builder()
                    .id(1L).contactNumber("1234567890").address("address").documentType("Passport")
                    .documentPath("path").status(KycStatus.APPROVED).userEmail("john@example.com").build();
            when(service.updateKycStatus(eq(1L), eq(KycStatus.APPROVED))).thenReturn(approved);

            mockMvc.perform(patch("/api/admin/kyc/1/status")
                    .param("status", "APPROVED")
                    .header("X-User-Email", "admin@example.com")
                    .header("X-User-Role", "ROLE_ADMIN")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should get KYC counts successfully for ADMIN")
        void getKycCounts_success() throws Exception {
            Map<String, Long> counts = Map.of("total", 50L, "PENDING", 10L);
            when(service.getKycCounts()).thenReturn(counts);

            mockMvc.perform(get("/api/admin/kyc/count")
                    .header("X-User-Email", "admin@example.com")
                    .header("X-User-Role", "ROLE_ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(50))
                    .andExpect(jsonPath("$.PENDING").value(10));
        }
    }
}
