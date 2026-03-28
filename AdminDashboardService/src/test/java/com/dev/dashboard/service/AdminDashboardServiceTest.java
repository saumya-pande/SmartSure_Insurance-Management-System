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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceTest {

    @Mock private AuthClient authClient;
    @Mock private PolicyClient policyClient;
    @Mock private ClaimsClient claimsClient;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    private final String ADMIN_EMAIL = "admin@smartsure.com";
    private final String ADMIN_ROLE = "ROLE_ADMIN";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(ADMIN_EMAIL);
    }

    @Test
    void getDashboard_Success() {
        when(authClient.getUserCounts(anyString(), anyString())).thenReturn(Map.of("total", 10L, "active", 8L));
        when(authClient.getKycCounts(anyString(), anyString())).thenReturn(Map.of("total", 5L, "PENDING", 2L));
        when(policyClient.getPolicyCounts(anyString(), anyString())).thenReturn(Map.of("total", 20L, "ACTIVE", 15L));
        when(policyClient.getRevenue(anyString(), anyString())).thenReturn(Map.of("total", 1000.0));
        when(claimsClient.getClaimCounts(anyString(), anyString())).thenReturn(Map.of("total", 3L, "APPROVED", 1L));
        when(claimsClient.getPayouts(anyString(), anyString())).thenReturn(Map.of("total", 200.0));

        DashboardResponse response = adminDashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(10L, response.getTotalUsers());
        assertEquals(800.0, response.getTotalRevenue()); // 1000 - 200
        assertEquals(1L, response.getClaimsByStatus().get("APPROVED"));
    }

    @Test
    void getUsers_Success() {
        Page<UserResponse> page = new PageImpl<>(List.of(new UserResponse()));
        when(authClient.getUsers(anyString(), anyString(), any(), anyBoolean(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        Page<UserResponse> result = adminDashboardService.getUsers("test@test.com", "Test", Role.CUSTOMER, true, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void toggleUserStatus_Success() {
        UserResponse response = new UserResponse();
        when(authClient.toggleUserStatus(anyLong(), anyBoolean(), anyString(), anyString())).thenReturn(response);

        UserResponse result = adminDashboardService.toggleUserStatus(1L, true);

        assertNotNull(result);
    }

    @Test
    void getKyc_Success() {
        Page<KycResponse> page = new PageImpl<>(List.of(new KycResponse()));
        when(authClient.getKyc(any(), anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        Page<KycResponse> result = adminDashboardService.getKyc(KycStatus.PENDING, "test@test.com", 0, 10);

        assertNotNull(result);
    }

    @Test
    void updateKycStatus_Success() {
        KycResponse response = new KycResponse();
        when(authClient.updateKycStatus(anyLong(), any(), anyString(), anyString())).thenReturn(response);

        KycResponse result = adminDashboardService.updateKycStatus(1L, KycStatus.APPROVED);

        assertNotNull(result);
    }

    @Test
    void createPolicy_Success() {
        BasicPolicyResponse response = new BasicPolicyResponse();
        when(policyClient.createPolicy(any(), anyString(), anyString())).thenReturn(response);

        BasicPolicyResponse result = adminDashboardService.createPolicy(new BasicPolicyRequest());

        assertNotNull(result);
    }

    @Test
    void deletePolicy_Success() {
        doNothing().when(policyClient).deletePolicy(anyLong(), anyString(), anyString());

        adminDashboardService.deletePolicy(1L);

        verify(policyClient, times(1)).deletePolicy(eq(1L), eq(ADMIN_ROLE), eq(ADMIN_EMAIL));
    }
    
    @Test
    void updatePolicy_Success() {
        BasicPolicyResponse response = new BasicPolicyResponse();
        when(policyClient.updatePolicy(anyLong(), any(), anyString(), anyString())).thenReturn(response);

        BasicPolicyResponse result = adminDashboardService.updatePolicy(1L, new BasicPolicyRequest());

        assertNotNull(result);
    }

    @Test
    void updatePolicyStatus_Success() {
        BasicPolicyResponse response = new BasicPolicyResponse();
        when(policyClient.updatePolicyStatus(anyLong(), any(), anyString(), anyString())).thenReturn(response);

        BasicPolicyResponse result = adminDashboardService.updatePolicyStatus(1L, PolicyStatus.ACTIVE);

        assertNotNull(result);
    }

    @Test
    void getBasicPolicies_Success() {
        Page<BasicPolicyResponse> page = new PageImpl<>(List.of(new BasicPolicyResponse()));
        when(policyClient.getBasicPolicies(any(), any(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(page);

        Page<BasicPolicyResponse> result = adminDashboardService.getBasicPolicies(PolicyType.HOME, PolicyStatus.ACTIVE, "Home", 0, 10, "id");

        assertNotNull(result);
    }

    @Test
    void getCustomerPolicies_Success() {
        Page<CustomerPolicyResponse> page = new PageImpl<>(List.of(new CustomerPolicyResponse()));
        when(policyClient.getCustomerPolicies(anyString(), any(), any(), anyDouble(), anyDouble(), anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(page);

        Page<CustomerPolicyResponse> result = adminDashboardService.getCustomerPolicies("test@test.com", PolicyType.HOME, PurchaseStatus.ACTIVE, 0.0, 1000.0, "2023-01-01", "2023-12-31", 0, 10, "id");

        assertNotNull(result);
    }

    @Test
    void getClaims_Success() {
        Page<ClaimResponse> page = new PageImpl<>(List.of(new ClaimResponse()));
        when(claimsClient.getClaims(any(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        Page<ClaimResponse> result = adminDashboardService.getClaims(ClaimStatus.SUBMITTED, "test@test.com", "2023-01-01", "2023-12-31", 0, 10);

        assertNotNull(result);
    }

    @Test
    void overrideClaimStatus_Success() {
        ClaimResponse response = new ClaimResponse();
        when(claimsClient.overrideClaimStatus(anyLong(), any(), anyString(), anyString())).thenReturn(response);

        ClaimResponse result = adminDashboardService.overrideClaimStatus(1L, ClaimStatus.APPROVED);

        assertNotNull(result);
    }
}
