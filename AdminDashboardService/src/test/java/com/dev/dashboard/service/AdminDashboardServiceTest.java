package com.dev.dashboard.service;

import com.dev.dashboard.clients.AuthClient;
import com.dev.dashboard.clients.ClaimsClient;
import com.dev.dashboard.clients.PolicyClient;
import com.dev.dashboard.dto.DashboardResponse;
import com.dev.dashboard.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private AuthClient authClient;
    @Mock
    private PolicyClient policyClient;
    @Mock
    private ClaimsClient claimsClient;

    @InjectMocks
    private AdminDashboardService adminService;

    @Test
    void getDashboard_success() {
        Map<String, Long> userCounts = new HashMap<>();
        userCounts.put("total", 100L);
        userCounts.put("active", 80L);

        Map<String, Long> kycCounts = new HashMap<>();
        kycCounts.put("PENDING", 5L);

        when(authClient.getUserCounts(anyString())).thenReturn(userCounts);
        when(authClient.getKycCounts(anyString())).thenReturn(kycCounts);
        when(policyClient.getPolicyCounts(anyString())).thenReturn(Collections.emptyMap());
        when(policyClient.getRevenue(anyString())).thenReturn(Collections.emptyMap());
        when(claimsClient.getClaimCounts(anyString())).thenReturn(Collections.emptyMap());

        DashboardResponse response = adminService.getDashboard();

        assertNotNull(response);
        assertEquals(100L, response.getTotalUsers());
        assertEquals(80L, response.getActiveUsers());
        assertEquals(5L, response.getPendingKyc());
    }

    @Test
    void getUsers_success() {
        Page<UserResponse> page = new PageImpl<>(Collections.emptyList());
        when(authClient.getUsers(any(), any(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(page);

        Page<UserResponse> result = adminService.getUsers(null, null, null, null, 0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void toggleUserStatus_success() {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setActive(false);

        when(authClient.toggleUserStatus(eq(1L), eq(false), anyString())).thenReturn(user);

        UserResponse result = adminService.toggleUserStatus(1L, false);

        assertNotNull(result);
        assertFalse(result.isActive());
    }
}
