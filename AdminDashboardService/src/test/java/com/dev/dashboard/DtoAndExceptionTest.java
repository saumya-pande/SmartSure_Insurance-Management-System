package com.dev.dashboard;

import com.dev.dashboard.dto.*;
import com.dev.dashboard.entity.*;
import com.dev.dashboard.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DtoAndExceptionTest {

    @Test
    void testBasicPolicyRequest() {
        BasicPolicyRequest request = new BasicPolicyRequest();
        request.setPolicyName("Name");
        request.setType(PolicyType.HOME);
        request.setBasePremium(100.0);
        request.setMaxPremium(200.0);

        assertEquals("Name", request.getPolicyName());
        assertEquals(PolicyType.HOME, request.getType());
        assertEquals(100.0, request.getBasePremium());
        assertEquals(200.0, request.getMaxPremium());
    }

    @Test
    void testResponses() {
        BasicPolicyResponse bp = new BasicPolicyResponse();
        bp.setId(1L);
        bp.setPolicyName("Test");
        assertEquals(1L, bp.getId());

        ClaimResponse cr = new ClaimResponse();
        cr.setId(2L);
        assertEquals(2L, cr.getId());

        CustomerPolicyResponse cpr = new CustomerPolicyResponse();
        cpr.setId(3L);
        assertEquals(3L, cpr.getId());

        DashboardResponse dr = DashboardResponse.builder().totalUsers(10L).build();
        assertEquals(10L, dr.getTotalUsers());

        KycResponse kr = new KycResponse();
        kr.setId(4L);
        assertEquals(4L, kr.getId());

        UserResponse ur = new UserResponse();
        ur.setId(5L);
        assertEquals(5L, ur.getId());
        
        RestPage<String> rp = new RestPage<>(List.of(), 0, 10, 0);
        assertNotNull(rp);
    }

    @Test
    void testEnums() {
        assertEquals(ClaimStatus.DRAFT, ClaimStatus.valueOf("DRAFT"));
        assertEquals(KycStatus.PENDING, KycStatus.valueOf("PENDING"));
        assertEquals(PolicyStatus.ACTIVE, PolicyStatus.valueOf("ACTIVE"));
        assertEquals(PolicyType.HOME, PolicyType.valueOf("HOME"));
        assertEquals(PurchaseStatus.ACTIVE, PurchaseStatus.valueOf("ACTIVE"));
        assertEquals(Role.CUSTOMER, Role.valueOf("CUSTOMER"));
    }

    @Test
    void testExceptions() {
        DownstreamServiceException dse = new DownstreamServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Message");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, dse.getStatus());

        EntityNotFoundException enfe = new EntityNotFoundException("Entity", "id", 1L);
        assertTrue(enfe.getMessage().contains("Entity"));

        InvalidOperationException ioe = new InvalidOperationException("Invalid");
        assertEquals("Invalid", ioe.getMessage());

        ServiceUnavailableException sue = new ServiceUnavailableException("Service");
        assertTrue(sue.getMessage().contains("Service"));
    }

    @Test
    void testErrorResponse() {
        ErrorResponse er = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Message")
                .path("/path")
                .timestamp(LocalDateTime.now())
                .errors(List.of(new ErrorResponse.ValidationError("field", "msg")))
                .build();
                
        assertEquals(400, er.getStatus());
        assertEquals("Bad Request", er.getError());
    }
}
