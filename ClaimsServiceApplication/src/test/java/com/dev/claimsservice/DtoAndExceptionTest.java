package com.dev.claimsservice;

import com.dev.claimsservice.dto.ClaimRequest;
import com.dev.claimsservice.dto.ClaimResponse;
import com.dev.claimsservice.dto.CustomerPolicyResponse;
import com.dev.claimsservice.entity.Claim;
import com.dev.claimsservice.entity.ClaimDocument;
import com.dev.claimsservice.entity.ClaimStatus;
import com.dev.claimsservice.entity.PurchaseStatus;
import com.dev.claimsservice.exception.DownstreamServiceException;
import com.dev.claimsservice.exception.DuplicateResourceException;
import com.dev.claimsservice.exception.EntityNotFoundException;
import com.dev.claimsservice.exception.ErrorResponse;
import com.dev.claimsservice.exception.InvalidOperationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DtoAndExceptionTest {

    @Test
    void testClaimRequest() {
        ClaimRequest request = new ClaimRequest();
        request.setCustomerPolicyId(1L);
        request.setClaimAmount(500.0);

        assertEquals(1L, request.getCustomerPolicyId());
        assertEquals(500.0, request.getClaimAmount());
    }

    @Test
    void testClaimResponse() {
        LocalDateTime now = LocalDateTime.now();
        ClaimResponse response = ClaimResponse.builder()
                .id(1L)
                .customerEmail("test@example.com")
                .customerPolicyId(2L)
                .claimAmount(150.0)
                .status(ClaimStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .documents(List.of())
                .build();

        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getCustomerEmail());
        assertEquals(2L, response.getCustomerPolicyId());
        assertEquals(150.0, response.getClaimAmount());
        assertEquals(ClaimStatus.DRAFT, response.getStatus());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
        assertEquals(0, response.getDocuments().size());
    }

    @Test
    void testCustomerPolicyResponse() {
        CustomerPolicyResponse response = new CustomerPolicyResponse();
        response.setId(1L);
        response.setCustomerEmail("email@example.com");
        response.setPremiumAmount(200.0);
        response.setStatus(PurchaseStatus.ACTIVE);

        assertEquals(1L, response.getId());
        assertEquals("email@example.com", response.getCustomerEmail());
        assertEquals(200.0, response.getPremiumAmount());
        assertEquals(PurchaseStatus.ACTIVE, response.getStatus());
    }

    @Test
    void testEntities() {
        Claim claim = Claim.builder()
                .id(1L)
                .customerEmail("cus@example.com")
                .customerPolicyId(2L)
                .claimAmount(120.0)
                .status(ClaimStatus.SUBMITTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .documents(List.of())
                .build();

        ClaimDocument document = ClaimDocument.builder()
                .id(1L)
                .claim(claim)
                .fileName("file.pdf")
                .filePath("/docs/file.pdf")
                .fileType("application/pdf")
                .build();

        assertNotNull(claim);
        assertNotNull(document);
        assertEquals("file.pdf", document.getFileName());
        assertEquals("/docs/file.pdf", document.getFilePath());
        assertEquals("application/pdf", document.getFileType());
        assertEquals(claim, document.getClaim());
    }

    @Test
    void testExceptions() {
        DownstreamServiceException ex1 = new DownstreamServiceException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "service", "msg");
        assertNotNull(ex1.getMessage());

        DuplicateResourceException ex2 = new DuplicateResourceException("res", "field", "val");
        assertNotNull(ex2.getMessage());

        EntityNotFoundException ex3 = new EntityNotFoundException("Entity", "id", 1L);
        assertNotNull(ex3.getMessage());

        InvalidOperationException ex4 = new InvalidOperationException("msg");
        assertEquals("msg", ex4.getMessage());
    }

    @Test
    void testErrorResponse() {
        ErrorResponse resp = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Message")
                .path("/path")
                .timestamp(LocalDateTime.now())
                .errors(List.of(new ErrorResponse.ValidationError("field", "msg")))
                .build();

        assertEquals(400, resp.getStatus());
        assertEquals("Bad Request", resp.getError());
        assertEquals("Message", resp.getMessage());
        assertEquals("/path", resp.getPath());
        assertNotNull(resp.getTimestamp());
        assertEquals(1, resp.getErrors().size());
        assertEquals("field", resp.getErrors().get(0).getField());
        assertEquals("msg", resp.getErrors().get(0).getMessage());
    }
}
