package com.dev.claimsservice.service;

import com.dev.claimsservice.client.PolicyClient;
import com.dev.claimsservice.dto.*;
import com.dev.claimsservice.entity.*;
import com.dev.claimsservice.exception.InvalidOperationException;
import com.dev.claimsservice.mapper.ClaimMapper;
import com.dev.claimsservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepo;
    @Mock
    private ClaimDocumentRepository docRepo;
    @Mock
    private PolicyClient policyClient;
    @Mock
    private ClaimMapper mapper;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ClaimService claimService;

    @TempDir
    Path tempDir;

    private final String customerEmail = "customer@example.com";
    private ClaimRequest claimRequest;
    private CustomerPolicyResponse policyResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(claimService, "UPLOAD_DIR", tempDir.toString());
        
        claimRequest = new ClaimRequest();
        claimRequest.setCustomerPolicyId(1L);
        claimRequest.setClaimAmount(500.0);

        policyResponse = new CustomerPolicyResponse();
        policyResponse.setId(1L);
        policyResponse.setCustomerEmail(customerEmail);
        policyResponse.setStatus(PurchaseStatus.ACTIVE);
    }

    @Test
    void createDraft_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test content".getBytes());
        List<MultipartFile> files = List.of(file);

        when(policyClient.getPolicyById(anyLong(), anyString(), anyString())).thenReturn(policyResponse);
        when(claimRepo.save(any(Claim.class))).thenAnswer(i -> {
            Claim c = i.getArgument(0);
            c.setId(10L);
            return c;
        });

        ClaimResponse response = claimService.createDraft(customerEmail, claimRequest, files);
        
        assertNotNull(response);
        assertEquals(10L, response.getId());

        verify(claimRepo, times(1)).save(any(Claim.class));
        verify(docRepo, times(1)).save(any(ClaimDocument.class));
    }

    @Test
    void createDraft_failure_notActive() {
        policyResponse.setStatus(PurchaseStatus.EXPIRED);
        when(policyClient.getPolicyById(anyLong(), anyString(), anyString())).thenReturn(policyResponse);

        assertThrows(InvalidOperationException.class, () -> 
            claimService.createDraft(customerEmail, claimRequest, Collections.emptyList())
        );
    }

    @Test
    void submit_success() {
        Claim claim = new Claim();
        claim.setId(10L);
        claim.setCustomerEmail(customerEmail);
        claim.setStatus(ClaimStatus.DRAFT);

        when(claimRepo.findById(10L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);

        claimService.submit(customerEmail, 10L);

        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());
    }

    @Test
    void submit_failure_wrongStatus() {
        Claim claim = new Claim();
        claim.setId(10L);
        claim.setCustomerEmail(customerEmail);
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepo.findById(10L)).thenReturn(Optional.of(claim));

        assertThrows(InvalidOperationException.class, () -> 
            claimService.submit(customerEmail, 10L)
        );
    }

    @Test
    void startReview_success() {
        Claim claim = new Claim();
        claim.setId(10L);
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepo.findById(10L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);

        claimService.updateStatus(10L, ClaimStatus.UNDER_REVIEW);

        assertEquals(ClaimStatus.UNDER_REVIEW, claim.getStatus());
    }

    @Test
    void updateStatus_approve_success() {
        Claim claim = new Claim();
        claim.setId(10L);
        claim.setStatus(ClaimStatus.UNDER_REVIEW);

        when(claimRepo.findById(10L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);

        claimService.updateStatus(10L, ClaimStatus.APPROVED);

        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
    }

    @Test
    void close_success() {
        Claim claim = new Claim();
        claim.setId(10L);
        claim.setStatus(ClaimStatus.APPROVED);

        when(claimRepo.findById(10L)).thenReturn(Optional.of(claim));
        when(claimRepo.save(any(Claim.class))).thenReturn(claim);

        claimService.updateStatus(10L, ClaimStatus.CLOSED);

        assertEquals(ClaimStatus.CLOSED, claim.getStatus());
    }
}
