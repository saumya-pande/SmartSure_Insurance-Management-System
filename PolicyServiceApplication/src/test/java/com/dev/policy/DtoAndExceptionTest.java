package com.dev.policy;

import com.dev.policy.client.KycResponse;
import com.dev.policy.dto.BasicPolicyRequest;
import com.dev.policy.dto.BasicPolicyResponse;
import com.dev.policy.dto.CustomerPolicyResponse;
import com.dev.policy.dto.PurchasePolicyRequest;
import com.dev.policy.entity.BasicPolicy;
import com.dev.policy.entity.CustomerPolicy;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.entity.PurchaseStatus;
import com.dev.policy.exception.DuplicatePropertyInsuranceException;
import com.dev.policy.exception.ErrorResponse;
import com.dev.policy.exception.InvalidFieldValueException;
import com.dev.policy.exception.InvalidOperationException;
import com.dev.policy.exception.MissingRequiredFieldException;
import com.dev.policy.exception.PolicyNotActiveException;
import com.dev.policy.exception.PolicyNotFoundException;
import com.dev.policy.exception.PremiumOutOfRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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
    void testBasicPolicyResponse() {
        BasicPolicyResponse response = BasicPolicyResponse.builder()
                .id(1L)
                .policyName("Name")
                .type(PolicyType.HOME)
                .basePremium(100.0)
                .maxPremium(200.0)
                .status(PolicyStatus.ACTIVE)
                .build();

        assertEquals(1L, response.getId());
        assertEquals("Name", response.getPolicyName());
        assertEquals(PolicyStatus.ACTIVE, response.getStatus());
    }

    @Test
    void testCustomerPolicyResponse() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusYears(1);

        CustomerPolicyResponse response = CustomerPolicyResponse.builder()
                .id(1L)
                .holderName("Holder")
                .customerEmail("email@example.com")
                .premiumAmount(150.0)
                .startDate(start)
                .endDate(end)
                .propertyIdentifier("Prop1")
                .policyType(PolicyType.HOME)
                .status(PurchaseStatus.ACTIVE)
                .policyName("BasePolicyName")
                .build();

        assertEquals(1L, response.getId());
        assertEquals("Holder", response.getHolderName());
        assertEquals("email@example.com", response.getCustomerEmail());
        assertEquals("BasePolicyName", response.getPolicyName());
        assertEquals(PolicyType.HOME, response.getPolicyType());
    }

    @Test
    void testPurchasePolicyRequest() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusYears(1);

        PurchasePolicyRequest request = new PurchasePolicyRequest();
        request.setBasicPolicyId(1L);
        request.setHolderName("Holder");
        request.setPremiumAmount(150.0);
        request.setStartDate(start);
        request.setEndDate(end);
        request.setPropertyIdentifier("Prop1");

        assertEquals(1L, request.getBasicPolicyId());
        assertEquals("Holder", request.getHolderName());
        assertEquals(150.0, request.getPremiumAmount());
        assertEquals(start, request.getStartDate());
        assertEquals(end, request.getEndDate());
        assertEquals("Prop1", request.getPropertyIdentifier());
    }

    @Test
    void testEntities() {
        BasicPolicy basic = BasicPolicy.builder()
                .id(1L)
                .policyName("P1")
                .type(PolicyType.VEHICLE)
                .basePremium(10.0)
                .maxPremium(20.0)
                .status(PolicyStatus.ACTIVE)
                .build();
        
        CustomerPolicy customer = CustomerPolicy.builder()
                .id(1L)
                .customerEmail("E")
                .holderName("H")
                .premiumAmount(15.0)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .propertyIdentifier("P")
                .policyType(PolicyType.VEHICLE)
                .status(PurchaseStatus.ACTIVE)
                .basicPolicy(basic)
                .build();

        assertNotNull(basic);
        assertNotNull(customer);
        assertEquals("P1", customer.getBasicPolicy().getPolicyName());
    }

    @Test
    void testExceptions() {
        DuplicatePropertyInsuranceException reqEx1 = new DuplicatePropertyInsuranceException("Prop1", PolicyType.HOME);
        assertNotNull(reqEx1.getMessage());

        InvalidFieldValueException reqEx2 = new InvalidFieldValueException("field", "val", "msg");
        assertNotNull(reqEx2.getMessage());

        InvalidOperationException reqEx3 = new InvalidOperationException("msg");
        assertEquals("msg", reqEx3.getMessage());

        MissingRequiredFieldException reqEx4 = new MissingRequiredFieldException("field");
        assertNotNull(reqEx4.getMessage());

        PolicyNotActiveException reqEx5 = new PolicyNotActiveException(1L);
        assertNotNull(reqEx5.getMessage());

        PolicyNotFoundException reqEx6 = new PolicyNotFoundException(1L);
        assertNotNull(reqEx6.getMessage());
        
        PolicyNotFoundException reqEx6_string = new PolicyNotFoundException("msg");
        assertNotNull(reqEx6_string.getMessage());

        PremiumOutOfRangeException reqEx7 = new PremiumOutOfRangeException(50.0, 100.0, 200.0);
        assertNotNull(reqEx7.getMessage());
    }

    @Test
    void testErrorResponse() {
        ErrorResponse resp = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("MSG")
                .path("/path")
                .timestamp(LocalDateTime.now())
                .errors(java.util.List.of(new ErrorResponse.ValidationError("field", "msg")))
                .build();
                
        assertEquals(400, resp.getStatus());
        assertEquals("Bad Request", resp.getError());
        assertEquals("MSG", resp.getMessage());
        assertEquals("/path", resp.getPath());
        assertNotNull(resp.getTimestamp());
        assertEquals(1, resp.getErrors().size());
        assertEquals("field", resp.getErrors().get(0).getField());
        assertEquals("msg", resp.getErrors().get(0).getMessage());
    }
    
    @Test
    void testKycResponse() {
        KycResponse resp = new KycResponse();
        resp.setId(1L);
        resp.setContactNumber("123");
        resp.setDocumentType("PASSPORT");
        resp.setAddress("ADDR");
        resp.setStatus("APPROVED");
        resp.setUserEmail("EMAIL");
        
        assertEquals(1L, resp.getId());
        assertEquals("123", resp.getContactNumber());
        assertEquals("PASSPORT", resp.getDocumentType());
        assertEquals("ADDR", resp.getAddress());
        assertEquals("APPROVED", resp.getStatus());
        assertEquals("EMAIL", resp.getUserEmail());
    }

    @Test
    void testRestPage() {
        com.dev.policy.dto.RestPage<String> restPage1 = new com.dev.policy.dto.RestPage<>(java.util.List.of("A", "B"), 0, 10, 2L);
        assertEquals(2, restPage1.getContent().size());
        assertEquals(2L, restPage1.getTotalElements());

        org.springframework.data.domain.Page<String> springPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of("C", "D"));
        com.dev.policy.dto.RestPage<String> restPage2 = new com.dev.policy.dto.RestPage<>(springPage);
        assertEquals(2, restPage2.getContent().size());

        com.dev.policy.dto.RestPage<String> restPage3 = new com.dev.policy.dto.RestPage<>(
                java.util.List.of("E", "F"), 0, 10, 2L, null, true, 1, null, true, 2);
        assertEquals(2, restPage3.getContent().size());
    }
}
