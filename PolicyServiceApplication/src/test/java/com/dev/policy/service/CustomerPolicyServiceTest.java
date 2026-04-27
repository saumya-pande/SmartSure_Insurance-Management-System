package com.dev.policy.service;

import com.dev.policy.client.KycClient;
import com.dev.policy.client.KycResponse;
import com.dev.policy.config.RabbitMQConfig;
import com.dev.policy.dto.CustomerPolicyResponse;
import com.dev.policy.dto.PurchasePolicyRequest;
import com.dev.policy.entity.BasicPolicy;
import com.dev.policy.entity.CustomerPolicy;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.entity.PurchaseStatus;
import com.dev.policy.exception.*;
import com.dev.policy.repository.BasicPolicyRepository;
import com.dev.policy.repository.CustomerPolicyRepository;
import feign.FeignException;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CustomerPolicyServiceTest {

    @Mock
    private CustomerPolicyRepository repo;

    @Mock
    private BasicPolicyRepository basicPolicyRepo;

    @Mock
    private KycClient kycClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CustomerPolicyService customerPolicyService;

    private AutoCloseable closeable;

    private PurchasePolicyRequest request;
    private BasicPolicy basicPolicy;
    private CustomerPolicy customerPolicy;
    private KycResponse kycResponse;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        request = new PurchasePolicyRequest();
        request.setBasicPolicyId(1L);
        request.setHolderName("John Doe");
        request.setPremiumAmount(200.0);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusYears(1));
        request.setPropertyIdentifier("PROP-123");

        basicPolicy = BasicPolicy.builder()
                .id(1L)
                .policyName("Home Protect")
                .type(PolicyType.HOME)
                .basePremium(100.0)
                .maxPremium(500.0)
                .status(PolicyStatus.ACTIVE)
                .build();

        customerPolicy = CustomerPolicy.builder()
                .id(100L)
                .customerEmail("john@example.com")
                .holderName("John Doe")
                .premiumAmount(200.0)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .propertyIdentifier("PROP-123")
                .policyType(PolicyType.HOME)
                .status(PurchaseStatus.ACTIVE)
                .basicPolicy(basicPolicy)
                .build();

        kycResponse = new KycResponse();
        kycResponse.setStatus("APPROVED");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void testPurchase_Success() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        when(basicPolicyRepo.findById(1L)).thenReturn(Optional.of(basicPolicy));
        when(repo.existsByPropertyIdentifierAndPolicyType(anyString(), any())).thenReturn(false);
        when(repo.save(any(CustomerPolicy.class))).thenReturn(customerPolicy);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        CustomerPolicyResponse response = customerPolicyService.purchase("john@example.com", "CUSTOMER", request);

        assertNotNull(response);
        assertEquals("John Doe", response.getHolderName());
        verify(repo, times(1)).save(any(CustomerPolicy.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_PURCHASED), anyMap());
    }

    @Test
    void testPurchase_KycNotApproved() {
        kycResponse.setStatus("PENDING");
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);

        assertThrows(InvalidOperationException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_KycNotFound() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenThrow(mock(FeignException.NotFound.class));

        assertThrows(InvalidOperationException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_KycForbidden() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenThrow(mock(FeignException.Forbidden.class));

        assertThrows(InvalidOperationException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_MissingBasicPolicyId() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        request.setBasicPolicyId(null);
        assertThrows(MissingRequiredFieldException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_MissingPropertyIdentifier() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        request.setPropertyIdentifier(null);
        assertThrows(MissingRequiredFieldException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
        
        request.setPropertyIdentifier("");
        assertThrows(MissingRequiredFieldException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_MissingPremiumAmount() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        request.setPremiumAmount(null);
        assertThrows(MissingRequiredFieldException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_BasicPolicyNotFound() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        when(basicPolicyRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_BasicPolicyNotActive() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        basicPolicy.setStatus(PolicyStatus.INACTIVE);
        when(basicPolicyRepo.findById(1L)).thenReturn(Optional.of(basicPolicy));

        assertThrows(PolicyNotActiveException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_PremiumTooLow() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        request.setPremiumAmount(50.0);
        when(basicPolicyRepo.findById(1L)).thenReturn(Optional.of(basicPolicy));

        assertThrows(PremiumOutOfRangeException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_PremiumTooHigh() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        request.setPremiumAmount(600.0);
        when(basicPolicyRepo.findById(1L)).thenReturn(Optional.of(basicPolicy));

        assertThrows(PremiumOutOfRangeException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_DuplicateProperty() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        when(basicPolicyRepo.findById(1L)).thenReturn(Optional.of(basicPolicy));
        when(repo.existsByPropertyIdentifierAndPolicyType(anyString(), any())).thenReturn(true);

        assertThrows(DuplicatePropertyInsuranceException.class, () -> customerPolicyService.purchase("john@example.com", "CUSTOMER", request));
    }

    @Test
    void testPurchase_RabbitMQExceptionIgnored() {
        when(kycClient.getMyKyc(anyString(), anyString())).thenReturn(kycResponse);
        when(basicPolicyRepo.findById(1L)).thenReturn(Optional.of(basicPolicy));
        when(repo.existsByPropertyIdentifierAndPolicyType(anyString(), any())).thenReturn(false);
        when(repo.save(any(CustomerPolicy.class))).thenReturn(customerPolicy);
        
        doThrow(new RuntimeException("RabbitMQ connection failed"))
            .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        CustomerPolicyResponse response = customerPolicyService.purchase("john@example.com", "CUSTOMER", request);
        assertNotNull(response); 
        assertEquals("John Doe", response.getHolderName());
    }

    @Test
    void testGetMyPolicies_Success() {
        Page<CustomerPolicy> page = new PageImpl<>(List.of(customerPolicy));
        when(repo.findByCustomerEmail(eq("john@example.com"), any(Pageable.class))).thenReturn(page);

        Page<CustomerPolicyResponse> response = customerPolicyService.getMyPolicies("john@example.com", PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetMyPolicies_NotFound() {
        Page<CustomerPolicy> page = new PageImpl<>(Collections.emptyList());
        when(repo.findByCustomerEmail(eq("john@example.com"), any(Pageable.class))).thenReturn(page);

        assertThrows(PolicyNotFoundException.class, () -> customerPolicyService.getMyPolicies("john@example.com", PageRequest.of(0, 10)));
    }

    @Test
    void testGetAllSimple() {
        Page<CustomerPolicy> page = new PageImpl<>(List.of(customerPolicy));
        when(repo.findAll(any(Pageable.class))).thenReturn(page);

        Page<CustomerPolicyResponse> response = customerPolicyService.getAll(PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetAllWithFilters_NoFilters() {
        Page<CustomerPolicy> page = new PageImpl<>(List.of(customerPolicy));
        when(repo.findAll(any(Pageable.class))).thenReturn(page);

        Page<CustomerPolicyResponse> response = customerPolicyService.getAll(null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetAllWithFilters_WithFilters() {
        Page<CustomerPolicy> page = new PageImpl<>(List.of(customerPolicy));
        when(repo.findByFilters(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        Page<CustomerPolicyResponse> response = customerPolicyService.getAll("test@email.com", PolicyType.HOME, PurchaseStatus.ACTIVE, 100.0, 500.0, "2023-01-01", "2023-12-31", PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetById_Success() {
        when(repo.findById(100L)).thenReturn(Optional.of(customerPolicy));

        CustomerPolicyResponse response = customerPolicyService.getById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void testGetById_NotFound() {
        when(repo.findById(100L)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> customerPolicyService.getById(100L));
    }

    @Test
    void testGetCustomerPolicyCounts() {
        when(repo.count()).thenReturn(10L);
        when(repo.countByStatus(PurchaseStatus.ACTIVE)).thenReturn(6L);
        when(repo.countByPolicyType(PolicyType.HOME)).thenReturn(5L);
        when(repo.countByPolicyType(PolicyType.VEHICLE)).thenReturn(5L);

        Map<String, Long> counts = customerPolicyService.getCustomerPolicyCounts();

        assertNotNull(counts);
        assertEquals(10L, counts.get("sold"));
        assertEquals(6L, counts.get("soldActive"));
        assertEquals(5L, counts.get("HOME"));
        assertEquals(5L, counts.get("VEHICLE"));
    }

    @Test
    void testGetRevenue_Success() {
        when(repo.sumPremiumAmount()).thenReturn(5000.0);

        Map<String, Double> revenue = customerPolicyService.getRevenue();

        assertNotNull(revenue);
        assertEquals(5000.0, revenue.get("total"));
    }

    @Test
    void testGetRevenue_Null() {
        when(repo.sumPremiumAmount()).thenReturn(null);

        Map<String, Double> revenue = customerPolicyService.getRevenue();

        assertNotNull(revenue);
        assertEquals(0.0, revenue.get("total"));
    }
}
