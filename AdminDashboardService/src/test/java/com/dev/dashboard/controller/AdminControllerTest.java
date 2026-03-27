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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "ADMIN")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.config.import="
})
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDashboardService adminDashboardService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse dummyUser;
    private KycResponse dummyKyc;
    private BasicPolicyResponse dummyPolicy;
    private CustomerPolicyResponse dummyCustomerPolicy;
    private ClaimResponse dummyClaim;

    @BeforeEach
    void setUp() {
        dummyUser = new UserResponse();
        dummyUser.setId(1L);
        dummyUser.setEmail("admin@test.com");
        dummyUser.setRole(Role.ADMIN);

        dummyKyc = new KycResponse();
        dummyKyc.setId(1L);
        dummyKyc.setStatus(KycStatus.APPROVED);

        dummyPolicy = new BasicPolicyResponse();
        dummyPolicy.setId(1L);
        dummyPolicy.setPolicyName("Auto Protect");

        dummyCustomerPolicy = new CustomerPolicyResponse();
        dummyCustomerPolicy.setId(1L);
        dummyCustomerPolicy.setCustomerEmail("customer@test.com");

        dummyClaim = new ClaimResponse();
        dummyClaim.setId(1L);
        dummyClaim.setClaimAmount(10000.0);
    }

    @Test
    void testGetDashboard() throws Exception {
        DashboardResponse mockResponse = DashboardResponse.builder()
                .totalUsers(100L)
                .totalRevenue(50000.0)
                .build();
        when(adminDashboardService.getDashboard()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.totalRevenue").value(50000.0));
        
        verify(adminDashboardService).getDashboard();
    }

    @Test
    void testGetUsers() throws Exception {
        Page<UserResponse> page = new PageImpl<>(Collections.singletonList(dummyUser));
        when(adminDashboardService.getUsers(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("admin@test.com"));
    }

    @Test
    void testToggleUserStatus() throws Exception {
        when(adminDashboardService.toggleUserStatus(1L, false)).thenReturn(dummyUser);

        mockMvc.perform(patch("/api/dashboard/users/1/status")
                .param("active", "false")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void testGetKyc() throws Exception {
        Page<KycResponse> page = new PageImpl<>(Collections.singletonList(dummyKyc));
        when(adminDashboardService.getKyc(any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/kyc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"));
    }

    @Test
    void testUpdateKycStatus() throws Exception {
        when(adminDashboardService.updateKycStatus(1L, KycStatus.REJECTED)).thenReturn(dummyKyc);

        mockMvc.perform(patch("/api/dashboard/kyc/1/status")
                .param("status", "REJECTED")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testCreatePolicy() throws Exception {
        BasicPolicyRequest request = new BasicPolicyRequest();
        request.setPolicyName("Auto Protect");
        request.setType(PolicyType.VEHICLE);
        request.setBasePremium(100.0);
        request.setMaxPremium(1000.0);
        when(adminDashboardService.createPolicy(any(BasicPolicyRequest.class))).thenReturn(dummyPolicy);

        mockMvc.perform(post("/api/dashboard/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyName").value("Auto Protect"));
    }

    @Test
    void testUpdatePolicy() throws Exception {
        BasicPolicyRequest request = new BasicPolicyRequest();
        request.setPolicyName("Auto Protect");
        request.setType(PolicyType.VEHICLE);
        request.setBasePremium(100.0);
        request.setMaxPremium(1000.0);
        when(adminDashboardService.updatePolicy(anyLong(), any(BasicPolicyRequest.class))).thenReturn(dummyPolicy);

        mockMvc.perform(put("/api/dashboard/policies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testDeletePolicy() throws Exception {
        mockMvc.perform(delete("/api/dashboard/policies/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Policy deleted"));
        
        verify(adminDashboardService).deletePolicy(1L);
    }

    @Test
    void testUpdatePolicyStatus() throws Exception {
        when(adminDashboardService.updatePolicyStatus(1L, PolicyStatus.INACTIVE)).thenReturn(dummyPolicy);

        mockMvc.perform(patch("/api/dashboard/policies/1/status")
                .param("status", "INACTIVE")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBasicPolicies() throws Exception {
        Page<BasicPolicyResponse> page = new PageImpl<>(Collections.singletonList(dummyPolicy));
        when(adminDashboardService.getBasicPolicies(any(), any(), any(), anyInt(), anyInt(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].policyName").value("Auto Protect"));
    }

    @Test
    void testGetCustomerPolicies() throws Exception {
        Page<CustomerPolicyResponse> page = new PageImpl<>(Collections.singletonList(dummyCustomerPolicy));
        when(adminDashboardService.getCustomerPolicies(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/policies/purchased"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerEmail").value("customer@test.com"));
    }

    @Test
    void testGetClaims() throws Exception {
        Page<ClaimResponse> page = new PageImpl<>(Collections.singletonList(dummyClaim));
        when(adminDashboardService.getClaims(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].claimAmount").value(10000.0));
    }

    @Test
    void testOverrideClaimStatus() throws Exception {
        when(adminDashboardService.overrideClaimStatus(1L, ClaimStatus.REJECTED)).thenReturn(dummyClaim);

        mockMvc.perform(patch("/api/dashboard/claims/1/status")
                .param("status", "REJECTED")
                .with(csrf()))
                .andExpect(status().isOk());
    }

}
