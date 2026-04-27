package com.dev.policy.service;

import com.dev.policy.dto.BasicPolicyRequest;
import com.dev.policy.dto.BasicPolicyResponse;
import com.dev.policy.dto.RestPage;
import com.dev.policy.entity.BasicPolicy;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.exception.InvalidFieldValueException;
import com.dev.policy.exception.MissingRequiredFieldException;
import com.dev.policy.exception.PolicyNotFoundException;
import com.dev.policy.repository.BasicPolicyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BasicPolicyServiceTest {

    @Mock
    private BasicPolicyRepository repo;

    @InjectMocks
    private BasicPolicyService basicPolicyService;

    private AutoCloseable closeable;

    private BasicPolicy basicPolicy;
    private BasicPolicyRequest request;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        basicPolicy = BasicPolicy.builder()
                .id(1L)
                .policyName("Home Protection")
                .type(PolicyType.HOME)
                .basePremium(100.0)
                .maxPremium(500.0)
                .status(PolicyStatus.ACTIVE)
                .build();

        request = new BasicPolicyRequest();
        request.setPolicyName("Home Protection");
        request.setType(PolicyType.HOME);
        request.setBasePremium(100.0);
        request.setMaxPremium(500.0);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void testCreate_Success() {
        when(repo.save(any(BasicPolicy.class))).thenReturn(basicPolicy);

        BasicPolicyResponse response = basicPolicyService.create(request);

        assertNotNull(response);
        assertEquals("Home Protection", response.getPolicyName());
        assertEquals(PolicyType.HOME, response.getType());
        verify(repo, times(1)).save(any(BasicPolicy.class));
    }

    @Test
    void testCreate_MissingPolicyName() {
        request.setPolicyName("");
        assertThrows(MissingRequiredFieldException.class, () -> basicPolicyService.create(request));
        
        request.setPolicyName(null);
        assertThrows(MissingRequiredFieldException.class, () -> basicPolicyService.create(request));
    }

    @Test
    void testCreate_MissingType() {
        request.setType(null);
        assertThrows(MissingRequiredFieldException.class, () -> basicPolicyService.create(request));
    }

    @Test
    void testCreate_MissingBasePremium() {
        request.setBasePremium(null);
        assertThrows(MissingRequiredFieldException.class, () -> basicPolicyService.create(request));
    }

    @Test
    void testCreate_MissingMaxPremium() {
        request.setMaxPremium(null);
        assertThrows(MissingRequiredFieldException.class, () -> basicPolicyService.create(request));
    }

    @Test
    void testCreate_BaseGreaterThanMax() {
        request.setBasePremium(600.0);
        request.setMaxPremium(500.0);
        assertThrows(InvalidFieldValueException.class, () -> basicPolicyService.create(request));
    }

    @Test
    void testUpdate_Success() {
        when(repo.findById(1L)).thenReturn(Optional.of(basicPolicy));
        when(repo.save(any(BasicPolicy.class))).thenReturn(basicPolicy);

        request.setPolicyName("Updated Home Protection");
        BasicPolicyResponse response = basicPolicyService.update(1L, request);

        assertNotNull(response);
        verify(repo, times(1)).save(any(BasicPolicy.class));
    }

    @Test
    void testUpdate_NotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> basicPolicyService.update(1L, request));
    }

    @Test
    void testUpdate_BaseGreaterThanMax() {
        when(repo.findById(1L)).thenReturn(Optional.of(basicPolicy));
        request.setBasePremium(600.0);
        request.setMaxPremium(500.0);

        assertThrows(InvalidFieldValueException.class, () -> basicPolicyService.update(1L, request));
    }

    @Test
    void testDelete_Success() {
        when(repo.findById(1L)).thenReturn(Optional.of(basicPolicy));

        basicPolicyService.delete(1L);

        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> basicPolicyService.delete(1L));
    }

    @Test
    void testUpdateStatus_Success() {
        when(repo.findById(1L)).thenReturn(Optional.of(basicPolicy));
        when(repo.save(any(BasicPolicy.class))).thenReturn(basicPolicy);

        BasicPolicyResponse response = basicPolicyService.updateStatus(1L, PolicyStatus.INACTIVE);

        assertNotNull(response);
        verify(repo, times(1)).save(any(BasicPolicy.class));
    }

    @Test
    void testUpdateStatus_NotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> basicPolicyService.updateStatus(1L, PolicyStatus.INACTIVE));
    }

    @Test
    void testGetActive_Success() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findByStatus(eq(PolicyStatus.ACTIVE), any(Pageable.class))).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getActive(PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetAll_StatusTypeAndName() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findByStatusAndTypeAndPolicyNameContainingIgnoreCase(any(), any(), any(), any())).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getAll(PolicyStatus.ACTIVE, PolicyType.HOME, "Home", PageRequest.of(0, 10));
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetAll_StatusAndType() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findByStatusAndType(any(), any(), any())).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getAll(PolicyStatus.ACTIVE, PolicyType.HOME, null, PageRequest.of(0, 10));
        assertNotNull(response);
    }

    @Test
    void testGetAll_StatusAndName() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findByStatusAndPolicyNameContainingIgnoreCase(any(), any(), any())).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getAll(PolicyStatus.ACTIVE, null, "Home", PageRequest.of(0, 10));
        assertNotNull(response);
    }

    @Test
    void testGetAll_TypeAndName() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findByTypeAndPolicyNameContainingIgnoreCase(any(), any(), any())).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getAll(null, PolicyType.HOME, "Home", PageRequest.of(0, 10));
        assertNotNull(response);
    }

    @Test
    void testGetAll_StatusOnly() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findByStatus(any(), any())).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getAll(PolicyStatus.ACTIVE, null, "  ", PageRequest.of(0, 10));
        assertNotNull(response);
    }

    @Test
    void testGetAll_TypeOnly() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findByType(any(), any())).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getAll(null, PolicyType.HOME, null, PageRequest.of(0, 10));
        assertNotNull(response);
    }

    @Test
    void testGetAll_NameOnly() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findByPolicyNameContainingIgnoreCase(any(), any())).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getAll(null, null, "Home", PageRequest.of(0, 10));
        assertNotNull(response);
    }

    @Test
    void testGetAll_NoFilters() {
        Page<BasicPolicy> page = new PageImpl<>(List.of(basicPolicy));
        when(repo.findAll(any(Pageable.class))).thenReturn(page);

        RestPage<BasicPolicyResponse> response = basicPolicyService.getAll(null, null, null, PageRequest.of(0, 10));
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetById_Success() {
        when(repo.findById(1L)).thenReturn(Optional.of(basicPolicy));

        BasicPolicyResponse response = basicPolicyService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testGetById_NotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> basicPolicyService.getById(1L));
    }

    @Test
    void testGetPolicyCounts() {
        when(repo.count()).thenReturn(10L);
        when(repo.countByStatus(PolicyStatus.ACTIVE)).thenReturn(6L);
        when(repo.countByStatus(PolicyStatus.INACTIVE)).thenReturn(4L);
        when(repo.countByType(PolicyType.HOME)).thenReturn(5L);
        when(repo.countByType(PolicyType.VEHICLE)).thenReturn(5L);

        Map<String, Long> counts = basicPolicyService.getPolicyCounts();

        assertNotNull(counts);
        assertEquals(10L, counts.get("total"));
        assertEquals(6L, counts.get("ACTIVE"));
        assertEquals(4L, counts.get("INACTIVE"));
        assertEquals(5L, counts.get("HOME"));
        assertEquals(5L, counts.get("VEHICLE"));
    }
}
