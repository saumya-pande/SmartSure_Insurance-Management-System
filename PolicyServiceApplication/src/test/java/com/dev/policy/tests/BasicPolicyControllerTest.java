package com.dev.policy.tests;


import com.dev.policy.controller.BasicPolicyController;
import com.dev.policy.dto.BasicPolicyRequest;
import com.dev.policy.dto.BasicPolicyResponse;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.exception.GlobalExceptionHandler;
import com.dev.policy.exception.PolicyNotFoundException;
import com.dev.policy.security.HeaderAuthFilter;
import com.dev.policy.service.BasicPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = BasicPolicyController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = HeaderAuthFilter.class
    )
)
public class BasicPolicyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean BasicPolicyService service;

    private BasicPolicyResponse sampleResponse;
    private BasicPolicyRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = BasicPolicyResponse.builder()
                .id(1L)
                .policyName("Home Basic")
                .type(PolicyType.HOME)
                .basePremium(1000.0)
                .maxPremium(5000.0)
                .status(PolicyStatus.ACTIVE)
                .build();

        validRequest = new BasicPolicyRequest();
        validRequest.setPolicyName("Home Basic");
        validRequest.setType(PolicyType.HOME);
        validRequest.setBasePremium(1000.0);
        validRequest.setMaxPremium(5000.0);
    }

    @Nested
    @DisplayName("POST /api/policies")
    class CreatePolicy {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 and created policy for ADMIN")
        void create_success() throws Exception {
            when(service.create(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/policies")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.policyName").value("Home Basic"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("should return 403 for CUSTOMER role")
        void create_forbidden() throws Exception {
            mockMvc.perform(post("/api/policies")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 401 for unauthenticated request")
        void create_unauthorized() throws Exception {
            mockMvc.perform(post("/api/policies")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/policies/{id}")
    class GetById {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 with policy")
        void getById_success() throws Exception {
            when(service.getById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/policies/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 404 when policy not found")
        void getById_notFound() throws Exception {
            when(service.getById(99L)).thenThrow(new PolicyNotFoundException(99L));

            mockMvc.perform(get("/api/policies/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 when id is not a number")
        void getById_typeMismatch() throws Exception {
            mockMvc.perform(get("/api/policies/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Type Mismatch"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/policies/{id}")
    class DeletePolicy {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 on successful delete")
        void delete_success() throws Exception {
            doNothing().when(service).delete(1L);

            mockMvc.perform(delete("/api/policies/1").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Policy deleted"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 404 when deleting non-existent policy")
        void delete_notFound() throws Exception {
            doThrow(new PolicyNotFoundException(99L)).when(service).delete(99L);

            mockMvc.perform(delete("/api/policies/99").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("should return 403 for CUSTOMER role")
        void delete_forbidden() throws Exception {
            mockMvc.perform(delete("/api/policies/1").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/policies (customer active list)")
    class GetActive {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("should return paginated active policies for CUSTOMER")
        void getActive_success() throws Exception {
            Page<BasicPolicyResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(service.getActive(any())).thenReturn(new com.dev.policy.dto.RestPage<>(page));

            mockMvc.perform(get("/api/policies")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].policyName").value("Home Basic"));
        }
    }
}