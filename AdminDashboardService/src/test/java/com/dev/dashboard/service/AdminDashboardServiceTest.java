package com.dev.dashboard.service;

import com.dev.dashboard.clients.AuthClient;
import com.dev.dashboard.clients.ClaimsClient;
import com.dev.dashboard.clients.PolicyClient;
import com.dev.dashboard.dto.*;
import com.dev.dashboard.entity.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceTest {

    @Mock
    private AuthClient authClient;

    @Mock
    private PolicyClient policyClient;

    @Mock
    private ClaimsClient claimsClient;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String ADMIN_EMAIL = "admin@test.com";

    @BeforeEach
    void setUp() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(ADMIN_EMAIL);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetDashboard() {
        when(authClient.getUserCounts(eq(ADMIN_ROLE), eq(ADMIN_EMAIL))).thenReturn(Map.of("total", 100L));
        when(authClient.getKycCounts(eq(ADMIN_ROLE), eq(ADMIN_EMAIL))).thenReturn(Map.of("total", 50L));
        when(policyClient.getPolicyCounts(eq(ADMIN_ROLE), eq(ADMIN_EMAIL))).thenReturn(Map.of("total", 200L));
        when(policyClient.getRevenue(eq(ADMIN_ROLE), eq(ADMIN_EMAIL))).thenReturn(Map.of("total", 50000.0));
        when(claimsClient.getClaimCounts(eq(ADMIN_ROLE), eq(ADMIN_EMAIL))).thenReturn(Map.of("total", 10L));
        when(claimsClient.getPayouts(eq(ADMIN_ROLE), eq(ADMIN_EMAIL))).thenReturn(Map.of("total", 5000.0));

        DashboardResponse response = adminDashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(100L, response.getTotalUsers());
        assertEquals(50L, response.getTotalKyc());
        assertEquals(200L, response.getTotalBasicPolicies());
        assertEquals(45000.0, response.getTotalRevenue());
        assertEquals(10L, response.getTotalClaims());
    }

    @Test
    void testGetUsers() {
        Page<UserResponse> mockPage = new PageImpl<>(Collections.singletonList(new UserResponse()));
        when(authClient.getUsers(any(), any(), any(), any(), anyInt(), anyInt(), eq(ADMIN_ROLE), eq(ADMIN_EMAIL)))
                .thenReturn(mockPage);

        Page<UserResponse> result = adminDashboardService.getUsers(null, null, null, null, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testToggleUserStatus() {
        UserResponse mockRes = new UserResponse();
        mockRes.setActive(false);
        when(authClient.toggleUserStatus(1L, false, ADMIN_ROLE, ADMIN_EMAIL)).thenReturn(mockRes);

        UserResponse result = adminDashboardService.toggleUserStatus(1L, false);
        assertNotNull(result);
        assertFalse(result.isActive());
    }

    @Test
    void testGetKyc() {
        Page<KycResponse> mockPage = new PageImpl<>(Collections.singletonList(new KycResponse()));
        when(authClient.getKyc(any(), any(), anyInt(), anyInt(), eq(ADMIN_ROLE), eq(ADMIN_EMAIL)))
                .thenReturn(mockPage);

        Page<KycResponse> result = adminDashboardService.getKyc(null, null, 0, 10);
        assertNotNull(result);
    }

    @Test
    void testUpdateKycStatus() {
        KycResponse mockRes = new KycResponse();
        mockRes.setStatus(KycStatus.APPROVED);
        when(authClient.updateKycStatus(1L, KycStatus.APPROVED, ADMIN_ROLE, ADMIN_EMAIL)).thenReturn(mockRes);

        KycResponse result = adminDashboardService.updateKycStatus(1L, KycStatus.APPROVED);
        assertNotNull(result);
        assertEquals(KycStatus.APPROVED, result.getStatus());
    }

    @Test
    void testCreatePolicy() {
        BasicPolicyRequest req = new BasicPolicyRequest();
        BasicPolicyResponse res = new BasicPolicyResponse();
        when(policyClient.createPolicy(req, ADMIN_ROLE, ADMIN_EMAIL)).thenReturn(res);

        BasicPolicyResponse result = adminDashboardService.createPolicy(req);
        assertNotNull(result);
    }

    @Test
    void testDeletePolicy() {
        doNothing().when(policyClient).deletePolicy(1L, ADMIN_ROLE, ADMIN_EMAIL);
        adminDashboardService.deletePolicy(1L);
        verify(policyClient).deletePolicy(1L, ADMIN_ROLE, ADMIN_EMAIL);
    }
}
