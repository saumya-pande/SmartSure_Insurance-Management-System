package com.dev.policy.tests;


import com.dev.policy.entity.*;
import com.dev.policy.repository.BasicPolicyRepository;
import com.dev.policy.repository.CustomerPolicyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerPolicyRepositoryTest {

    @Autowired CustomerPolicyRepository repo;
    @Autowired BasicPolicyRepository basicPolicyRepo;

    private BasicPolicy basicPolicy;

    @BeforeEach
    void setUp() {
        basicPolicy = basicPolicyRepo.save(BasicPolicy.builder()
                .policyName("Home Basic")
                .type(PolicyType.HOME)
                .basePremium(1000.0).maxPremium(5000.0)
                .status(PolicyStatus.ACTIVE).build());

        repo.save(CustomerPolicy.builder()
                .customerEmail("john@test.com")
                .holderName("John")
                .premiumAmount(2000.0)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .propertyIdentifier("FLAT-101")
                .policyType(PolicyType.HOME)
                .status(PurchaseStatus.ACTIVE)
                .basicPolicy(basicPolicy)
                .build());
    }

    @Test
    @DisplayName("findByCustomerEmail should return policies for given email")
    void findByCustomerEmail_success() {
        Page<CustomerPolicy> result = repo.findByCustomerEmail(
                "john@test.com", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPropertyIdentifier()).isEqualTo("FLAT-101");
    }

    @Test
    @DisplayName("findByCustomerEmail should return empty for unknown email")
    void findByCustomerEmail_unknown() {
        Page<CustomerPolicy> result = repo.findByCustomerEmail(
                "nobody@test.com", PageRequest.of(0, 10));
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("existsByPropertyIdentifierAndPolicyType should return true for duplicate")
    void existsByProperty_duplicate() {
        assertThat(repo.existsByPropertyIdentifierAndPolicyType(
                "FLAT-101", PolicyType.HOME)).isTrue();
    }

    @Test
    @DisplayName("existsByPropertyIdentifierAndPolicyType should return false for new property")
    void existsByProperty_notDuplicate() {
        assertThat(repo.existsByPropertyIdentifierAndPolicyType(
                "FLAT-999", PolicyType.HOME)).isFalse();
    }

    @Test
    @DisplayName("same property with different type should not be a duplicate")
    void existsByProperty_samePropertyDifferentType() {
        assertThat(repo.existsByPropertyIdentifierAndPolicyType(
                "FLAT-101", PolicyType.VEHICLE)).isFalse();
    }
}
