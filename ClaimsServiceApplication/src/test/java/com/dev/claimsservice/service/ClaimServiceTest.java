package com.dev.claimsservice.service;

import com.dev.claimsservice.client.PolicyClient;
import com.dev.claimsservice.config.RabbitMQConfig;
import com.dev.claimsservice.dto.ClaimRequest;
import com.dev.claimsservice.dto.ClaimResponse;
import com.dev.claimsservice.dto.CustomerPolicyResponse;
import com.dev.claimsservice.entity.Claim;
import com.dev.claimsservice.entity.ClaimDocument;
import com.dev.claimsservice.entity.ClaimStatus;
import com.dev.claimsservice.entity.PurchaseStatus;
import com.dev.claimsservice.exception.EntityNotFoundException;
import com.dev.claimsservice.exception.InvalidOperationException;
import com.dev.claimsservice.mapper.ClaimMapper;
import com.dev.claimsservice.repository.ClaimDocumentRepository;
import com.dev.claimsservice.repository.ClaimRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ClaimServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ClaimRepository claimRepo;

    @Mock
    private ClaimDocumentRepository docRepo;

    @Mock
    private PolicyClient policyClient;

    @Mock
    private ClaimMapper mapper;

    @InjectMocks
    private ClaimService claimService;

    private AutoCloseable closeable;

    private ClaimRequest claimRequest;
    private CustomerPolicyResponse policyResponse;
    private Claim claim;
    private ClaimResponse claimResponse;
    private MockMultipartFile validFile;
    private MockMultipartFile invalidFile;
    private String tempUploadDir;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        tempUploadDir = System.getProperty("java.io.tmpdir") + File.separator + "claims_test_" + System.currentTimeMillis();
        ReflectionTestUtils.setField(claimService, "UPLOAD_DIR", tempUploadDir);

        claimRequest = new ClaimRequest();
        claimRequest.setCustomerPolicyId(10L);
        claimRequest.setClaimAmount(500.0);

        policyResponse = new CustomerPolicyResponse();
        policyResponse.setCustomerEmail("user@example.com");
        policyResponse.setStatus(PurchaseStatus.ACTIVE);

        claim = Claim.builder()
                .id(100L)
                .customerEmail("user@example.com")
                .customerPolicyId(10L)
                .claimAmount(500.0)
                .status(ClaimStatus.DRAFT)
                .documents(new ArrayList<>())
                .build();

        claimResponse = ClaimResponse.builder()
                .id(100L)
                .customerEmail("user@example.com")
                .status(ClaimStatus.DRAFT)
                .claimAmount(500.0)
                .build();

        validFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());
        invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "dummy content".getBytes());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        File testDir = new File(tempUploadDir);
        if (testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            testDir.delete();
        }
    }

    @Test
    void testCreateDraft_Success() throws Exception {
        when(policyClient.getPolicyById(10L, "user@example.com", "ROLE_CUSTOMER")).thenReturn(policyResponse);
        when(claimRepo.existsByCustomerPolicyIdAndStatusNot(10L, ClaimStatus.REJECTED)).thenReturn(false);
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);
        when(docRepo.save(any(ClaimDocument.class))).thenReturn(new ClaimDocument());
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        ClaimResponse response = claimService.createDraft("user@example.com", claimRequest, List.of(validFile));

        assertNotNull(response);
        assertEquals(ClaimStatus.DRAFT, response.getStatus());
        verify(claimRepo, times(1)).save(any(Claim.class));
        verify(docRepo, times(1)).save(any(ClaimDocument.class));
    }

    @Test
    void testCreateDraft_WrongCustomer() {
        policyResponse.setCustomerEmail("other@example.com");
        when(policyClient.getPolicyById(10L, "user@example.com", "ROLE_CUSTOMER")).thenReturn(policyResponse);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                claimService.createDraft("user@example.com", claimRequest, List.of(validFile)));
        assertEquals("Policy does not belong to this customer", ex.getMessage());
    }

    @Test
    void testCreateDraft_PolicyNotActive() {
        policyResponse.setStatus(PurchaseStatus.EXPIRED);
        when(policyClient.getPolicyById(10L, "user@example.com", "ROLE_CUSTOMER")).thenReturn(policyResponse);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                claimService.createDraft("user@example.com", claimRequest, List.of(validFile)));
        assertEquals("Policy is not active", ex.getMessage());
    }

    @Test
    void testCreateDraft_ClaimAlreadyExists() {
        when(policyClient.getPolicyById(10L, "user@example.com", "ROLE_CUSTOMER")).thenReturn(policyResponse);
        when(claimRepo.existsByCustomerPolicyIdAndStatusNot(10L, ClaimStatus.REJECTED)).thenReturn(true);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                claimService.createDraft("user@example.com", claimRequest, List.of(validFile)));
        assertTrue(ex.getMessage().contains("already been submitted"));
    }

    @Test
    void testCreateDraft_InvalidFileType() throws Exception {
        when(policyClient.getPolicyById(10L, "user@example.com", "ROLE_CUSTOMER")).thenReturn(policyResponse);
        when(claimRepo.existsByCustomerPolicyIdAndStatusNot(10L, ClaimStatus.REJECTED)).thenReturn(false);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                claimService.createDraft("user@example.com", claimRequest, List.of(validFile, invalidFile)));
        assertTrue(ex.getMessage().contains("Invalid file type"));
    }

    @Test
    void testCreateDraft_NullContentType() throws Exception {
        when(policyClient.getPolicyById(10L, "user@example.com", "ROLE_CUSTOMER")).thenReturn(policyResponse);
        when(claimRepo.existsByCustomerPolicyIdAndStatusNot(10L, ClaimStatus.REJECTED)).thenReturn(false);
        MockMultipartFile nullMimeFile = new MockMultipartFile("file", "test.pdf", null, "dummy".getBytes());

        assertThrows(InvalidOperationException.class, () ->
                claimService.createDraft("user@example.com", claimRequest, List.of(nullMimeFile)));
    }

    @Test
    void testSubmit_Success() {
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);
        claimResponse.setStatus(ClaimStatus.SUBMITTED);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        ClaimResponse response = claimService.submit("user@example.com", 100L);

        assertNotNull(response);
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus()); 
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_CLAIM_SUBMITTED), anyMap());
    }

    @Test
    void testSubmit_NotDraft() {
        claim.setStatus(ClaimStatus.SUBMITTED);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                claimService.submit("user@example.com", 100L));
        assertEquals("Only DRAFT claims can be submitted", ex.getMessage());
    }

    @Test
    void testSubmit_ClaimNotFound() {
        when(claimRepo.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> claimService.submit("user@example.com", 100L));
    }

    @Test
    void testSubmit_WrongCustomer() {
        claim.setCustomerEmail("other@example.com");
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                claimService.submit("user@example.com", 100L));
        assertEquals("Claim does not belong to this customer", ex.getMessage());
    }
    
    @Test
    void testSubmit_RabbitMQException() {
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);
        doThrow(new RuntimeException("Rabbit MQ Error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        ClaimResponse response = claimService.submit("user@example.com", 100L);
        assertNotNull(response);
    }

    @Test
    void testUpdateStatus_ToUnderReview_Success() {
        claim.setStatus(ClaimStatus.SUBMITTED);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        ClaimResponse response = claimService.updateStatus(100L, ClaimStatus.UNDER_REVIEW);
        assertNotNull(response);
    }

    @Test
    void testUpdateStatus_ToUnderReview_Fail() {
        claim.setStatus(ClaimStatus.DRAFT);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                claimService.updateStatus(100L, ClaimStatus.UNDER_REVIEW));
        assertEquals("Only SUBMITTED claims can be reviewed", ex.getMessage());
    }

    @Test
    void testUpdateStatus_ToApproved_Success() {
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        ClaimResponse response = claimService.updateStatus(100L, ClaimStatus.APPROVED);
        assertNotNull(response);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_CLAIM_STATUS_UPDATED), anyMap());
    }
    
    @Test
    void testUpdateStatus_ToApproved_Fail() {
        claim.setStatus(ClaimStatus.SUBMITTED);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                claimService.updateStatus(100L, ClaimStatus.APPROVED));
        assertEquals("Claim must be UNDER_REVIEW to approve or reject", ex.getMessage());
    }

    @Test
    void testUpdateStatus_ToClosed_Success() {
        claim.setStatus(ClaimStatus.APPROVED);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        ClaimResponse response = claimService.updateStatus(100L, ClaimStatus.CLOSED);
        assertNotNull(response);
    }

    @Test
    void testUpdateStatus_ToClosed_Fail() {
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));

        assertThrows(InvalidOperationException.class, () -> claimService.updateStatus(100L, ClaimStatus.CLOSED));
    }

    @Test
    void testUpdateStatus_InvalidTargetStatus() {
        claim.setStatus(ClaimStatus.DRAFT);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));

        assertThrows(InvalidOperationException.class, () -> claimService.updateStatus(100L, ClaimStatus.DRAFT));
    }
    
    @Test
    void testUpdateStatusRabbitException() {
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        when(claimRepo.findById(100L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);
        doThrow(new RuntimeException("Rabbit Exception")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        ClaimResponse response = claimService.updateStatus(100L, ClaimStatus.APPROVED);
        assertNotNull(response);        
    }

    @Test
    void testGetMyClaims() {
        Page<Claim> page = new PageImpl<>(List.of(claim));
        when(claimRepo.findByCustomerEmail(eq("user@example.com"), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        Page<ClaimResponse> result = claimService.getMyClaims("user@example.com", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetAll_WithDates() {
        Page<Claim> page = new PageImpl<>(List.of(claim));
        when(claimRepo.findByFilters(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        Page<ClaimResponse> result = claimService.getAll(ClaimStatus.DRAFT, "user@example.com", "2024-01-01", "2024-12-31", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }
    
    @Test
    void testGetAll_StatusAndEmail() {
        Page<Claim> page = new PageImpl<>(List.of(claim));
        when(claimRepo.findByStatusAndCustomerEmailContaining(any(), any(), any())).thenReturn(page);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        Page<ClaimResponse> result = claimService.getAll(ClaimStatus.DRAFT, "user@example.com", null, "", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetAll_StatusOnly() {
        Page<Claim> page = new PageImpl<>(List.of(claim));
        when(claimRepo.findByStatus(any(), any())).thenReturn(page);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        Page<ClaimResponse> result = claimService.getAll(ClaimStatus.DRAFT, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }
    
    @Test
    void testGetAll_EmailOnly() {
        Page<Claim> page = new PageImpl<>(List.of(claim));
        when(claimRepo.findByCustomerEmailContaining(any(), any())).thenReturn(page);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        Page<ClaimResponse> result = claimService.getAll(null, "user@example.com", null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }
    
    @Test
    void testGetAll_NoFilters() {
        Page<Claim> page = new PageImpl<>(List.of(claim));
        when(claimRepo.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        Page<ClaimResponse> result = claimService.getAll(null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetClaimCounts() {
        when(claimRepo.count()).thenReturn(10L);
        when(claimRepo.countByStatus(any())).thenReturn(2L);

        Map<String, Long> counts = claimService.getClaimCounts();

        assertEquals(10L, counts.get("total"));
        assertEquals(2L, counts.get(ClaimStatus.DRAFT.name()));
    }

    @Test
    void testGetApprovedPayouts() {
        when(claimRepo.sumClaimAmountByStatus(ClaimStatus.APPROVED)).thenReturn(1000.0);

        Map<String, Double> payouts = claimService.getApprovedPayouts();

        assertEquals(1000.0, payouts.get("total"));
    }

    @Test
    void testGetApprovedPayouts_Null() {
        when(claimRepo.sumClaimAmountByStatus(ClaimStatus.APPROVED)).thenReturn(null);

        Map<String, Double> payouts = claimService.getApprovedPayouts();

        assertEquals(0.0, payouts.get("total"));
    }
}
