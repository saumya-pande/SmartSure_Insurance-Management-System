package com.dev.claimsservice.controller;

import com.dev.claimsservice.dto.ClaimResponse;
import com.dev.claimsservice.entity.ClaimStatus;
import com.dev.claimsservice.security.HeaderAuthFilter;
import com.dev.claimsservice.security.SecurityConfig;
import com.dev.claimsservice.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaimController.class)
@Import({SecurityConfig.class, HeaderAuthFilter.class})
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClaimService claimService;

    @Test
    void createDraft_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.pdf", "application/pdf", "test data".getBytes());
        ClaimResponse response = new ClaimResponse();
        response.setId(10L);
        response.setStatus(ClaimStatus.DRAFT);

        when(claimService.createDraft(anyString(), any(), anyList())).thenReturn(response);

        mockMvc.perform(multipart("/api/claims/draft")
                .file(file)
                .param("customerPolicyId", "1")
                .param("claimAmount", "500.0")
                .header("X-User-Email", "customer@example.com")
                .header("X-User-Role", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void submit_success() throws Exception {
        ClaimResponse response = new ClaimResponse();
        response.setId(10L);
        response.setStatus(ClaimStatus.SUBMITTED);

        when(claimService.submit(eq("customer@example.com"), eq(10L))).thenReturn(response);

        mockMvc.perform(patch("/api/claims/10/submit")
                .header("X-User-Email", "customer@example.com")
                .header("X-User-Role", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void getMyClaims_success() throws Exception {
        Page<ClaimResponse> page = new PageImpl<>(Collections.emptyList());
        when(claimService.getMyClaims(eq("customer@example.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/claims/my")
                .header("X-User-Email", "customer@example.com")
                .header("X-User-Role", "ROLE_CUSTOMER"))
                .andExpect(status().isOk());
    }

    @Test
    void admin_updateStatus_review_success() throws Exception {
        ClaimResponse response = new ClaimResponse();
        response.setId(10L);
        response.setStatus(ClaimStatus.UNDER_REVIEW);

        when(claimService.updateStatus(10L, ClaimStatus.UNDER_REVIEW)).thenReturn(response);

        mockMvc.perform(patch("/api/claims/10/status")
                .param("status", "UNDER_REVIEW")
                .header("X-User-Email", "admin@example.com")
                .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
    }

    @Test
    void admin_forbidden_for_customer() throws Exception {
        mockMvc.perform(patch("/api/claims/10/status")
                .param("status", "UNDER_REVIEW")
                .header("X-User-Email", "customer@example.com")
                .header("X-User-Role", "ROLE_CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}
