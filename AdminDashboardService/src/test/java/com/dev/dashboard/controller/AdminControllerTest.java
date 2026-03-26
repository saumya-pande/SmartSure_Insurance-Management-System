package com.dev.dashboard.controller;

import com.dev.dashboard.dto.DashboardResponse;
import com.dev.dashboard.dto.UserResponse;
import com.dev.dashboard.security.HeaderAuthFilter;
import com.dev.dashboard.security.SecurityConfig;
import com.dev.dashboard.service.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, HeaderAuthFilter.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminDashboardService adminDashboardService;

    @Test
    void getDashboard_success() throws Exception {
        DashboardResponse response = DashboardResponse.builder()
                .totalUsers(10L)
                .build();

        when(adminDashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard")
                .header("X-User-Email", "admin@example.com")
                .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10));
    }

    @Test
    void getUsers_success() throws Exception {
        Page<UserResponse> page = new PageImpl<>(Collections.emptyList());
        when(adminDashboardService.getUsers(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/dashboard/users")
                .header("X-User-Email", "admin@example.com")
                .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void toggleUserStatus_success() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setActive(true);

        when(adminDashboardService.toggleUserStatus(eq(1L), eq(true))).thenReturn(response);

        mockMvc.perform(patch("/api/dashboard/users/1/status")
                .param("active", "true")
                .header("X-User-Email", "admin@example.com")
                .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void forbidden_for_customer() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                .header("X-User-Email", "customer@example.com")
                .header("X-User-Role", "ROLE_CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}
