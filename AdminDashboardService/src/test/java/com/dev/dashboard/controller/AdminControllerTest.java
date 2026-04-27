package com.dev.dashboard.controller;

import com.dev.dashboard.dto.*;
import com.dev.dashboard.entity.*;
import com.dev.dashboard.service.AdminDashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters for controller unit test
public class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AdminDashboardService adminDashboardService;
    @MockBean private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDashboard_Success() throws Exception {
        DashboardResponse response = DashboardResponse.builder().totalUsers(10L).build();
        when(adminDashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_Success() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(new UserResponse()));
        when(adminDashboardService.getUsers(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleUserStatus_Success() throws Exception {
        UserResponse response = new UserResponse();
        when(adminDashboardService.toggleUserStatus(anyLong(), anyBoolean())).thenReturn(response);

        mockMvc.perform(patch("/api/dashboard/users/1/status")
                        .param("active", "true")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getKyc_Success() throws Exception {
        Page<KycResponse> page = new PageImpl<>(List.of(new KycResponse()));
        when(adminDashboardService.getKyc(any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/kyc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateKycStatus_Success() throws Exception {
        KycResponse response = new KycResponse();
        when(adminDashboardService.updateKycStatus(anyLong(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/dashboard/kyc/1/status")
                        .param("status", "APPROVED")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPolicy_Success() throws Exception {
        BasicPolicyResponse response = new BasicPolicyResponse();
        when(adminDashboardService.createPolicy(any())).thenReturn(response);

        BasicPolicyRequest request = new BasicPolicyRequest();
        request.setPolicyName("Test Policy");
        request.setType(PolicyType.HOME);
        request.setBasePremium(100.0);
        request.setMaxPremium(1000.0);

        mockMvc.perform(post("/api/dashboard/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClaims_Success() throws Exception {
        Page<ClaimResponse> page = new PageImpl<>(List.of(new ClaimResponse()));
        when(adminDashboardService.getClaims(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/claims"))
                .andExpect(status().isOk());
    }
}
